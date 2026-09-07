package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.*;
import com.example.ems.appraisal.service.AppraisalCycleService;
import com.example.ems.appraisal.service.AppraisalEvaluationService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AppraisalConcurrencyIntegrationTest {

    @Autowired
    private AppraisalCycleService cycleService;

    @Autowired
    private AppraisalEvaluationService evaluationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    private Organization org;
    private Employee emp;
    private Employee hrEmp;
    private AppraisalCycle cycle;

    @BeforeEach
    public void setup() {
        long ts = System.currentTimeMillis();
        org = new Organization();
        org.setName("Concurrency Org " + ts);
        org.setOrganizationCode("CONC-" + ts);
        org = organizationRepository.save(org);

        TenantContext.setCurrentTenant(org.getId());

        Role adminRole = roleRepository.findByName("PLATFORM_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("PLATFORM_ADMIN");
                    r.setOrganization(org);
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("EMPLOYEE");
                    r.setOrganization(org);
                    return roleRepository.save(r);
                });

        emp = new Employee();
        emp.setOrganization(org);
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setEmail("john.conc." + ts + "@test.com");
        emp.setDepartment("Engineering");
        emp.setDesignation("Software Engineer");
        emp.setJoiningDate(LocalDate.now().minusMonths(18));
        emp = employeeRepository.save(emp);

        hrEmp = new Employee();
        hrEmp.setOrganization(org);
        hrEmp.setFirstName("Helen");
        hrEmp.setLastName("HR");
        hrEmp.setEmail("hr.conc." + ts + "@test.com");
        hrEmp.setDepartment("HR");
        hrEmp.setDesignation("HR Lead");
        hrEmp.setJoiningDate(LocalDate.now().minusMonths(36));
        hrEmp = employeeRepository.save(hrEmp);

        User hrUser = new User();
        hrUser.setWorkEmail(hrEmp.getEmail());
        hrUser.setRole(adminRole);
        hrUser.setOrganization(org);
        userRepository.save(hrUser);

        User empUser = new User();
        empUser.setWorkEmail(emp.getEmail());
        empUser.setRole(empRole);
        empUser.setOrganization(org);
        userRepository.save(empUser);

        AppraisalConfiguration config = new AppraisalConfiguration();
        config.setOrganization(org);
        config.setInitiationMode(AppraisalInitiationMode.HR_AND_EMPLOYEE);
        config.setEmployeeRequestEnabled(true);
        config.setMinServiceMonths(6);
        config.setActive(true);
        configRepository.save(config);

        cycle = new AppraisalCycle();
        cycle.setOrganization(org);
        cycle.setName("Concurrency Test Cycle " + ts);
        cycle.setStartDate(LocalDate.now());
        cycle.setEndDate(LocalDate.now().plusMonths(3));
        cycle.setStatus("OPEN");
        cycle = cycleRepository.save(cycle);
    }

    @AfterEach
    public void cleanup() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("CONC-01: Real Concurrent Race Condition - HR Batch Generation vs Simultaneous Creation")
    public void testConcurrentAppraisalCreationGuaranteesSingleRecord() throws Exception {
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger expectedRejectionsOrIgnored = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    TenantContext.setCurrentTenant(org.getId());
                    startLatch.await(); // Simultaneous release

                    if (index % 2 == 0) {
                        // Thread running HR batch generation
                        cycleService.generateAppraisalsForCycle(cycle.getId());
                        successfulCreations.incrementAndGet();
                    } else {
                        // Thread creating appraisal directly for employee in cycle
                        try {
                            Appraisal a = new Appraisal();
                            a.setOrganization(org);
                            a.setEmployee(emp);
                            a.setCycle(cycle);
                            a.setStatus(AppraisalStatus.CREATED);
                            a.setCurrentStageOrder(1);
                            a.setCreatedAt(LocalDateTime.now());
                            a.setUpdatedAt(LocalDateTime.now());
                            appraisalRepository.saveAndFlush(a);
                            successfulCreations.incrementAndGet();
                        } catch (Exception ex) {
                            // Caught DB constraint or concurrency rejection
                            expectedRejectionsOrIgnored.incrementAndGet();
                        }
                    }
                } catch (Exception ex) {
                    expectedRejectionsOrIgnored.incrementAndGet();
                } finally {
                    TenantContext.clear();
                    doneLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrency test timed out");

        TenantContext.setCurrentTenant(org.getId());
        List<Appraisal> allCycleAppraisals = appraisalRepository.findByCycleId(cycle.getId());

        List<Appraisal> empAppraisals = allCycleAppraisals.stream()
                .filter(a -> a.getEmployee().getId().equals(emp.getId()))
                .toList();

        // CRITICAL INVARIANT: Exactly 1 appraisal record exists for this employee in this cycle!
        assertEquals(1, empAppraisals.size(), "INVARIANT VIOLATION: Exactly 1 appraisal record must exist for the employee + cycle");
        assertEquals(emp.getId(), empAppraisals.get(0).getEmployee().getId());
    }

    @Test
    @DisplayName("CONC-02: Concurrent Self-Assessment Submissions - Only first submit transitions state")
    public void testConcurrentSelfAssessmentSubmissions() throws Exception {
        TenantContext.setCurrentTenant(org.getId());

        Appraisal appraisal = new Appraisal();
        appraisal.setOrganization(org);
        appraisal.setEmployee(emp);
        appraisal.setCycle(cycle);
        appraisal.setStatus(AppraisalStatus.CREATED);
        appraisal.setCurrentStageOrder(1);
        appraisal.setCreatedAt(LocalDateTime.now());
        appraisal.setUpdatedAt(LocalDateTime.now());
        appraisal = appraisalRepository.saveAndFlush(appraisal);

        final Long appraisalId = appraisal.getId();

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    TenantContext.setCurrentTenant(org.getId());
                    startLatch.await();

                    SelfAssessmentDto dto = new SelfAssessmentDto(4.5, "Strong", "Achieved", "None", null);
                    evaluationService.submitSelfAssessment(appraisalId, dto, emp);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    rejectedCount.incrementAndGet();
                } finally {
                    TenantContext.clear();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        TenantContext.setCurrentTenant(org.getId());
        Appraisal updated = appraisalRepository.findById(appraisalId).orElseThrow();
        assertEquals(AppraisalStatus.STAGE_REVIEW, updated.getStatus());
        assertEquals(1, successCount.get(), "Only exactly one self-assessment submission should succeed");
        assertEquals(threadCount - 1, rejectedCount.get(), "Subsequent concurrent submissions must be rejected");
    }
}
