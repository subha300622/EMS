package com.example.ems.leave;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeavePolicy;
import com.example.ems.leave.entity.LeaveRule;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeavePolicyRepository;
import com.example.ems.leave.repository.LeaveRuleRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.leave.service.LeaveBalanceService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentLeaveBalanceRaceConditionTest {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    private Employee employee;
    private LeaveType vacationLeaveType;

    @BeforeEach
    public void setUp() {
        organization = new Organization();
        organization.setName("Concurrency Org " + System.currentTimeMillis());
        organization.setOrganizationCode("ORG_CR_" + System.currentTimeMillis());
        organization = organizationRepository.save(organization);

        Employee manager = new Employee();
        manager.setEmployeeId("MGR_CR_" + System.currentTimeMillis());
        manager.setFullName("Concurrency Manager");
        manager.setEmail("mgr.cr." + System.currentTimeMillis() + "@test.com");
        manager.setDepartment("Engineering");
        manager.setOrganization(organization);
        manager = employeeRepository.save(manager);

        employee = new Employee();
        employee.setEmployeeId("EMP_CR_" + System.currentTimeMillis());
        employee.setFullName("Concurrency Test Employee");
        employee.setEmail("emp.cr." + System.currentTimeMillis() + "@test.com");
        employee.setDepartment("Engineering");
        employee.setManager(manager);
        employee.setOrganization(organization);
        employee = employeeRepository.save(employee);

        vacationLeaveType = new LeaveType();
        vacationLeaveType.setName("Vacation " + System.currentTimeMillis());
        vacationLeaveType.setDescription("Vacation Leave");
        vacationLeaveType.setDefaultDays(2);
        vacationLeaveType.setActive(true);
        vacationLeaveType.setOrganization(organization);
        vacationLeaveType = leaveTypeRepository.save(vacationLeaveType);

        LeavePolicy policy = new LeavePolicy();
        policy.setName("Policy " + System.currentTimeMillis());
        policy.setLeaveType(vacationLeaveType);
        policy.setAccrualType("ANNUAL");
        policy.setStatus("ACTIVE");
        policy.setOrganization(organization);
        leavePolicyRepository.save(policy);

        LeaveRule rule = new LeaveRule();
        rule.setLeaveType(vacationLeaveType);
        rule.setNoticePeriodDays(0);
        rule.setMaxConsecutiveDays(30);
        rule.setAllowNegativeBalance(false);
        rule.setAllowHalfDay(true);
        rule.setIncludeWeekends(false);
        rule.setIncludeHolidays(false);
        rule.setOrganization(organization);
        leaveRuleRepository.save(rule);

        // Initial Balance: Total = 2.0, Used = 0.0, Pending = 0.0, Available = 2.0
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee);
        balance.setLeaveType(vacationLeaveType);
        balance.setYear(2026);
        balance.setTotalEntitlement(2.0);
        balance.setUsedBalance(0.0);
        balance.setPendingBalance(0.0);
        balance.setOrganization(organization);
        leaveBalanceRepository.save(balance);
    }

    @Test
    @DisplayName("Pessimistic locking prevents balance overdraw: 4 concurrent 2-day requests against 2-day balance -> exactly 1 succeeds, 3 fail")
    public void testConcurrentLeaveBalanceReservationRaceCondition() throws InterruptedException {
        int concurrentThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);
        CountDownLatch readyLatch = new CountDownLatch(concurrentThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientBalanceCount = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < concurrentThreads; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Simultaneous burst
                    // Each thread requests 2 days
                    leaveBalanceService.reserveBalance(employee, vacationLeaveType, 2026, 2.0);
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("Insufficient leave balance")) {
                        insufficientBalanceCount.incrementAndGet();
                    } else {
                        unexpectedErrors.add(ex);
                    }
                } catch (Throwable t) {
                    unexpectedErrors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire all 4 threads simultaneously
        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Concurrent balance reservation threads did not finish in time");
        assertTrue(unexpectedErrors.isEmpty(), "Unexpected errors during concurrency test: " + unexpectedErrors);

        // Assertions: Exactly 1 succeeds, exactly 3 fail with insufficient balance
        assertEquals(1, successCount.get(), "Exactly one concurrent thread must succeed in reserving the 2-day balance");
        assertEquals(3, insufficientBalanceCount.get(), "Remaining 3 threads must be rejected due to insufficient balance");

        // Database Balance Verification
        LeaveBalance finalBalance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), vacationLeaveType.getId(), 2026
        ).orElseThrow();

        assertEquals(2.0, finalBalance.getTotalEntitlement(), "Total entitlement must remain 2.0");
        assertEquals(0.0, finalBalance.getUsedBalance(), "Used balance must be 0.0");
        assertEquals(2.0, finalBalance.getPendingBalance(), "Pending balance must be exactly 2.0");
        assertEquals(0.0, finalBalance.getAvailableBalance(), "Available balance must be exactly 0.0");

        // Invariant check: available >= 0, pending <= total
        assertTrue(finalBalance.getAvailableBalance() >= 0.0, "Available balance must never be negative");
        assertTrue(finalBalance.getPendingBalance() <= finalBalance.getTotalEntitlement(), "Pending balance must never exceed total entitlement");
    }
}
