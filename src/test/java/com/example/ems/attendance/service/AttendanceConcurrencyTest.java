package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.entity.GracePeriodType;
import com.example.ems.attendance.entity.ExceedGraceAction;
import com.example.ems.attendance.repository.AttendanceGraceUsageRepository;
import com.example.ems.attendance.repository.AttendancePolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import java.time.LocalTime;

import com.example.ems.attendance.exception.DuplicateCheckInException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.ems.attendance.repository.AttendanceLogRepository;

@SpringBootTest
public class AttendanceConcurrencyTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AttendancePolicyRepository attendancePolicyRepository;

    @Autowired
    private AttendanceGraceUsageRepository attendanceGraceUsageRepository;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        Organization org = organizationRepository.findAll().stream().findFirst().orElseGet(() -> {
            Organization o = new Organization();
            o.setName("Attendance Test Org");
            o.setOrganizationCode("ATT-TEST-ORG");
            return organizationRepository.save(o);
        });

        List<AttendancePolicy> existingPolicies = attendancePolicyRepository.findActivePoliciesForOrganization(org.getId());
        if (existingPolicies.isEmpty()) {
            AttendancePolicy policy = new AttendancePolicy();
            policy.setName("Test Attendance Policy");
            policy.setOrganization(org);
            policy.setOfficeStartTime(LocalTime.of(9, 0));
            policy.setOfficeEndTime(LocalTime.of(18, 0));
            policy.setGracePeriodMinutes(15);
            policy.setMinimumWorkingMinutes(480);
            policy.setHalfDayThreshold(240);
            policy.setLateThreshold(15);
            policy.setEarlyCheckoutThreshold(15);
            policy.setMaximumBreakMinutes(60);
            policy.setLateGraceMinutes(10);
            policy.setEarlyExitGraceMinutes(10);
            policy.setGraceOccurrencesPerPeriod(3);
            policy.setGracePeriodType(GracePeriodType.MONTHLY);
            policy.setAllowLateGrace(true);
            policy.setAllowEarlyExitGrace(true);
            policy.setExceedGraceAction(ExceedGraceAction.MARK_LATE);
            policy.setMaxMonthlyPermissions(4);
            policy.setMaxDailyPermissionMinutes(120);
            policy.setMaxMonthlyPermissionMinutes(480);
            policy.setStatus(AttendancePolicyStatus.ACTIVE);
            attendancePolicyRepository.save(policy);
        }

        // Create a unique employee for this test to avoid conflicting with seeded data
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        employee = new Employee();
        employee.setOrganization(org);
        employee.setFullName("Concurrency Test Employee");
        employee.setEmail("concurrency." + uniqueId + "@company.com");
        employee.setEmployeeId("EMP-CONC-" + uniqueId);
        employee.setPhone("555-" + uniqueId);
        employee.setGender("MALE");
        employee.setDob(LocalDate.of(1995, 5, 5));
        employee.setAddress("123 Test Street");
        employee.setEmergencyContact("9876543210");
        employee.setDepartment("Engineering");
        employee.setDesignation("Software Engineer");
        employee.setAnnualSalary(BigDecimal.valueOf(90000));
        employee.setJoiningDate(LocalDate.now());
        employee.setLocation("Remote");
        employee.setEmploymentType("FULL_TIME");
        employee.setStatus("ACTIVE");

        employee = employeeRepository.save(employee);
    }

    @AfterEach
    public void tearDown() {
        if (employee != null && employee.getId() != null) {
            attendanceGraceUsageRepository.deleteAll(attendanceGraceUsageRepository.findByEmployeeId(employee.getId()));
            attendanceRepository.deleteAll(attendanceRepository.findByEmployeeId(employee.getId()));
            attendanceLogRepository.deleteAll(attendanceLogRepository.findByEmployeeId(employee.getId()));
            employeeRepository.delete(employee);
        }
    }

    @Test
    public void testConcurrentCheckInsResultInSingleRecord() throws InterruptedException, ExecutionException {
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Future<String>> futures = new ArrayList<>();
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger collisionCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await(); // wait for start signal to execute simultaneously
                try {
                    attendanceService.checkIn(employee, "Punched in via Concurrency Test");
                    successCounter.incrementAndGet();
                    return "SUCCESS";
                } catch (DuplicateCheckInException e) {
                    // Service-layer duplicate guard fired — expected for losing threads
                    collisionCounter.incrementAndGet();
                    return "COLLISION:service:" + e.getMessage();
                } catch (Exception e) {
                    // DB-level constraint violation or any exception whose cause chain
                    // contains a DataIntegrityViolationException counts as a collision
                    Throwable cause = e;
                    boolean isConstraintViolation = false;
                    while (cause != null) {
                        if (cause instanceof DataIntegrityViolationException
                                || cause instanceof DuplicateCheckInException) {
                            isConstraintViolation = true;
                            break;
                        }
                        // Also match by message for legacy wrapper exceptions
                        if (cause.getMessage() != null && (
                                cause.getMessage().contains("Already checked in")
                                || cause.getMessage().contains("duplicate key")
                                || cause.getMessage().contains("uk_attendance")
                                || cause.getMessage().contains("unique constraint"))) {
                            isConstraintViolation = true;
                            break;
                        }
                        cause = cause.getCause();
                    }
                    if (isConstraintViolation) {
                        collisionCounter.incrementAndGet();
                        return "COLLISION:db:" + e.getClass().getSimpleName();
                    }
                    return "ERROR:" + e.getClass().getName() + ":" + e.getMessage();
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        // Wait for all worker threads to be ready at the gate
        readyLatch.await(5, TimeUnit.SECONDS);

        // Trigger concurrent execution
        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Output results
        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get());
        }
        System.out.println("ATTENDANCE CONCURRENCY RESULTS: " + results);

        // Verify thread results
        assertEquals(1, successCounter.get(), "Exactly one check-in must succeed");
        assertEquals(threadCount - 1, collisionCounter.get(), "All other concurrent attempts must be caught as collisions");

        // Verify final database state (the absolute truth)
        List<Attendance> savedRecords = attendanceRepository.findByEmployeeId(employee.getId());
        assertEquals(1, savedRecords.size(), "Database must contain exactly ONE attendance record for the employee and date");
        assertEquals("Punched in via Concurrency Test", savedRecords.get(0).getNotes());
    }
}
