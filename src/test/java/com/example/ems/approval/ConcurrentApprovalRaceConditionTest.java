package com.example.ems.approval;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentApprovalRaceConditionTest {

    @Autowired
    private ApprovalWorkflowEngineService workflowEngineService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private User testUser;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        Organization org = new Organization();
        org.setName("Concurrent Test Org " + System.currentTimeMillis());
        org = organizationRepository.save(org);

        testEmployee = new Employee();
        testEmployee.setFullName("Concurrent Test User");
        testEmployee.setEmail("concurrent_test_" + System.currentTimeMillis() + "@test.com");
        testEmployee.setEmployeeId("EMP-" + System.currentTimeMillis());
        testEmployee.setOrganization(org);
        testEmployee.setStatus("ACTIVE");
        testEmployee = employeeRepository.save(testEmployee);

        testUser = new User();
        testUser.setFullName(testEmployee.getFullName());
        testUser.setWorkEmail(testEmployee.getEmail());
        testUser.setUserId(testEmployee.getEmployeeId());
        testUser.setOrganization(org);
        testUser = userRepository.save(testUser);
    }

    @Test
    public void testConcurrentApprovalRaceCondition_ExactlyOneSucceeds() throws Exception {
        // Start workflow instance
        ApprovalWorkflowInstance instance = workflowEngineService.startWorkflow(
                WorkflowType.EXPENSE_APPROVAL,
                "EXPENSE",
                "EXP-RACE-100",
                testEmployee,
                null
        );
        assertNotNull(instance);

        String approvalTaskId = workflowEngineService.getInbox(testUser, WorkflowType.EXPENSE_APPROVAL, ApprovalStatus.PENDING, 0, 10)
                .getTasks().get(0).getTaskId();

        assertNotNull(approvalTaskId);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to fire simultaneously
                    workflowEngineService.approveTask(testUser, approvalTaskId, "Concurrent approve #" + index);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown(); // Release threads simultaneously
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly ONE approval request must succeed");
        assertEquals(9, failureCount.get(), "Remaining 9 approval requests must fail/be rejected by state machine or lock");
    }
}
