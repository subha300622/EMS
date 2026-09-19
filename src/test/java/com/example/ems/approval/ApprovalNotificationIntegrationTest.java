package com.example.ems.approval;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalActionRequiredEvent;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.entity.Notification;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.common.service.NotificationService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ApprovalNotificationIntegrationTest {

    @Autowired
    private ApprovalWorkflowEngineService workflowEngineService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Organization org;
    private User testUser;
    private Employee testEmployee;

    @BeforeEach
    public void setUp() {
        org = new Organization();
        org.setName("Notification Test Org " + System.currentTimeMillis());
        org.setOrganizationCode("ORG-NOTIF-" + System.currentTimeMillis());
        org = organizationRepository.save(org);

        testEmployee = new Employee();
        testEmployee.setFullName("Approval Approver Employee");
        testEmployee.setEmail("approver_" + System.currentTimeMillis() + "@test.com");
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
    public void testApprovalTaskTriggersInAppNotification_AfterCommit() {
        // Start an approval workflow in an explicit transaction so AFTER_COMMIT fires
        ApprovalWorkflowInstance instance = transactionTemplate.execute(status ->
                workflowEngineService.startWorkflow(
                        WorkflowType.LEAVE_APPROVAL,
                        "LEAVE",
                        "LV-9901",
                        testEmployee,
                        null
                )
        );

        assertNotNull(instance);

        // Verify In-App Notification was created for testUser
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertFalse(notifications.isEmpty(), "An in-app notification must be created for the approver");

        Notification notif = notifications.get(0);
        assertEquals(testUser.getId(), notif.getUser().getId());
        assertEquals("APPROVAL", notif.getType());
        assertEquals("HIGH", notif.getPriority());
        assertFalse(notif.isRead());
        assertTrue(notif.getTitle().contains("Leave Request Approval Required"));
        assertTrue(notif.getMessage().contains("LV-9901"));
    }

    @Test
    public void testNotificationFailureDoesNotAffectApprovalCommit() {
        // Create an employee with NO matching User account (notification creation will fail or be skipped)
        Employee orphanEmployee = new Employee();
        orphanEmployee.setFullName("Orphan Employee");
        orphanEmployee.setEmail("no_user_" + System.currentTimeMillis() + "@test.com");
        orphanEmployee.setEmployeeId("EMP-ORPHAN-" + System.currentTimeMillis());
        orphanEmployee.setOrganization(org);
        orphanEmployee.setStatus("ACTIVE");
        Employee savedOrphan = employeeRepository.save(orphanEmployee);

        // Start workflow assigned to orphanEmployee
        ApprovalWorkflowInstance instance = transactionTemplate.execute(status ->
                workflowEngineService.startWorkflow(
                        WorkflowType.EXPENSE_APPROVAL,
                        "EXPENSE",
                        "EXP-SAFE-01",
                        savedOrphan,
                        null
                )
        );

        // Verify that the approval workflow still committed successfully
        assertNotNull(instance);
        assertEquals(ApprovalStatus.IN_PROGRESS, instance.getStatus());

        // Verify direct manual call to createApprovalNotification with invalid data does not throw unhandled exception
        ApprovalActionRequiredEvent invalidEvent = new ApprovalActionRequiredEvent(
                this,
                "TASK-INVALID",
                "WFI-INVALID",
                WorkflowType.EXPENSE_APPROVAL,
                "EXPENSE",
                "EXP-INVALID",
                org.getId(),
                1,
                "Stage 1",
                99999999L // Non-existent employee
        );

        assertDoesNotThrow(() -> notificationService.createApprovalNotification(invalidEvent));
    }
}
