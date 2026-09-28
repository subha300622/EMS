package com.example.ems.common.service;

import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.notification.NotificationCommand;
import com.example.ems.common.dto.notification.NotificationRecipientContext;
import com.example.ems.common.entity.*;
import com.example.ems.common.repository.NotificationDeliveryRepository;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipientResolverAndNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApprovalTaskRepository approvalTaskRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryRepository deliveryRepository;

    @InjectMocks
    private RecipientResolver recipientResolver;

    @InjectMocks
    private NotificationService notificationService;

    private User userA;
    private User userB;
    private Employee empA;
    private Employee empB;
    private final Long ORG_ID = 1L;

    @BeforeEach
    public void setUp() {
        // Wire recipientResolver into notificationService
        org.springframework.test.util.ReflectionTestUtils.setField(notificationService, "recipientResolver", recipientResolver);

        userA = new User();
        userA.setId(101L);
        userA.setFullName("User A");
        userA.setWorkEmail("usera@test.com");
        userA.setOrganizationId(ORG_ID);
        userA.setStatus("ACTIVE");

        userB = new User();
        userB.setId(102L);
        userB.setFullName("User B");
        userB.setWorkEmail("userb@test.com");
        userB.setOrganizationId(ORG_ID);
        userB.setStatus("ACTIVE");

        empA = new Employee();
        empA.setId(201L);
        empA.setFullName("Employee A");
        empA.setEmail("usera@test.com");

        empB = new Employee();
        empB.setId(202L);
        empB.setFullName("Employee B");
        empB.setEmail("userb@test.com");
    }

    // ── 1. RecipientResolver Hierarchy Tests ─────────────────────────────────

    @Test
    @DisplayName("Priority 1: Explicit user ID resolves directly and suppresses lower priorities")
    public void testPriority1_ExplicitUserResolvesFirst() {
        when(userRepository.findByIdAndOrganizationId(101L, ORG_ID)).thenReturn(Optional.of(userA));

        NotificationRecipientContext context = NotificationRecipientContext.builder()
                .organizationId(ORG_ID)
                .explicitUserId(101L)
                .requiredPermission("LEAVE_APPROVE")
                .build();

        Set<User> recipients = recipientResolver.resolveRecipients(context);

        assertEquals(1, recipients.size());
        assertTrue(recipients.contains(userA));
        // Verify permission lookup was NOT invoked because explicit user was found
        verify(roleService, never()).hasPermission(anyString(), anyString());
    }

    @Test
    @DisplayName("Priority 2: Approval Task approver is resolved instead of all permission holders")
    public void testPriority2_ApprovalTaskApproverResolvesFirst() {
        ApprovalTask task = new ApprovalTask();
        task.setApprovalTaskId("TASK-999");
        task.setApprover(empB);

        when(approvalTaskRepository.findByApprovalTaskId("TASK-999")).thenReturn(Optional.of(task));
        when(employeeRepository.findById(202L)).thenReturn(Optional.of(empB));
        when(userRepository.findByWorkEmailAndOrganizationId("userb@test.com", ORG_ID)).thenReturn(Optional.of(userB));

        NotificationRecipientContext context = NotificationRecipientContext.builder()
                .organizationId(ORG_ID)
                .approvalTaskId("TASK-999")
                .requiredPermission("LEAVE_APPROVE")
                .build();

        Set<User> recipients = recipientResolver.resolveRecipients(context);

        assertEquals(1, recipients.size());
        assertTrue(recipients.contains(userB));
        // Critical: User A also has LEAVE_APPROVE, but User B is the step assignee, so only User B is returned!
        assertFalse(recipients.contains(userA));
        verify(roleService, never()).hasPermission(anyString(), anyString());
    }

    @Test
    @DisplayName("Priority 4: Manager hierarchy resolves reporting manager of requester")
    public void testPriority4_ManagerHierarchyResolves() {
        // EmpA's manager is EmpB
        empA.setManager(empB);
        when(employeeRepository.findById(201L)).thenReturn(Optional.of(empA));
        when(employeeRepository.findById(202L)).thenReturn(Optional.of(empB));
        when(userRepository.findByWorkEmailAndOrganizationId("userb@test.com", ORG_ID)).thenReturn(Optional.of(userB));

        NotificationRecipientContext context = NotificationRecipientContext.builder()
                .organizationId(ORG_ID)
                .requesterEmployeeId(201L)
                .resolveManager(true)
                .build();

        Set<User> recipients = recipientResolver.resolveRecipients(context);

        assertEquals(1, recipients.size());
        assertTrue(recipients.contains(userB));
    }

    @Test
    @DisplayName("Priority 5: Fallback to permission holders when no explicit assignee or approver exists")
    public void testPriority5_FallbackPermissionHolders() {
        when(userRepository.findByOrganizationId(ORG_ID)).thenReturn(List.of(userA, userB));
        when(roleService.hasPermission("usera@test.com", "LEAVE_APPROVE")).thenReturn(true);
        when(roleService.hasPermission("userb@test.com", "LEAVE_APPROVE")).thenReturn(false);

        NotificationRecipientContext context = NotificationRecipientContext.builder()
                .organizationId(ORG_ID)
                .requiredPermission("LEAVE_APPROVE")
                .build();

        Set<User> recipients = recipientResolver.resolveRecipients(context);

        assertEquals(1, recipients.size());
        assertTrue(recipients.contains(userA));
        assertFalse(recipients.contains(userB));
    }

    // ── 2. NotificationService Core Operations Tests ─────────────────────────

    @Test
    @DisplayName("create() creates notification and writes initial IN_APP delivery record")
    public void testCreateNotification_Success() {
        when(userRepository.findByIdAndOrganizationId(101L, ORG_ID)).thenReturn(Optional.of(userA));
        when(notificationRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(501L);
            return n;
        });

        NotificationCommand command = NotificationCommand.builder()
                .organizationId(ORG_ID)
                .recipientUserId(101L)
                .category(NotificationCategory.APPROVAL)
                .type(NotificationType.APPROVAL_ACTION_REQUIRED)
                .title("Leave approval required")
                .message("John Doe requested leave")
                .priority(NotificationPriority.HIGH)
                .actionRequired(true)
                .actionUrl("/leaves/842")
                .entityType("LEAVE_REQUEST")
                .entityId(842L)
                .idempotencyKey("APPROVAL:842:1:101")
                .build();

        Notification created = notificationService.create(command);

        assertNotNull(created);
        assertEquals(501L, created.getId());
        assertEquals(NotificationCategory.APPROVAL, created.getCategory());
        assertEquals("APPROVAL_ACTION_REQUIRED", created.getType());
        assertTrue(created.isActionRequired());
        assertEquals("/leaves/842", created.getActionUrl());
        assertEquals(ORG_ID, created.getOrganizationId());

        // Verify Delivery record was created
        verify(deliveryRepository, times(1)).save(argThat(delivery ->
                delivery.getNotification().getId().equals(501L) &&
                delivery.getRecipientUser().getId().equals(101L) &&
                delivery.getChannel() == DeliveryChannel.IN_APP &&
                delivery.getStatus() == DeliveryStatus.SENT
        ));
    }

    @Test
    @DisplayName("create() with existing idempotencyKey is a safe no-op and returns existing entity")
    public void testCreateNotification_IdempotencyDeduplication() {
        when(userRepository.findByIdAndOrganizationId(101L, ORG_ID)).thenReturn(Optional.of(userA));

        Notification existingNotif = new Notification();
        existingNotif.setId(777L);
        existingNotif.setIdempotencyKey("APPROVAL:842:1:101");
        when(notificationRepository.findByIdempotencyKey("APPROVAL:842:1:101")).thenReturn(Optional.of(existingNotif));

        NotificationCommand command = NotificationCommand.builder()
                .organizationId(ORG_ID)
                .recipientUserId(101L)
                .category(NotificationCategory.APPROVAL)
                .type(NotificationType.APPROVAL_ACTION_REQUIRED)
                .idempotencyKey("APPROVAL:842:1:101")
                .build();

        Notification result = notificationService.create(command);

        assertEquals(777L, result.getId());
        // Verify save was never called on duplicate
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(deliveryRepository, never()).save(any(NotificationDelivery.class));
    }

    @Test
    @DisplayName("markRead() updates read status and readAt timestamp")
    public void testMarkRead_Success() {
        Notification notif = new Notification();
        notif.setId(501L);
        notif.setUser(userA);
        notif.setOrganizationId(ORG_ID);
        notif.setRead(false);

        when(notificationRepository.findByIdAndUserIdAndOrganizationId(501L, 101L, ORG_ID))
                .thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification updated = notificationService.markRead(501L, 101L, ORG_ID);

        assertTrue(updated.isRead());
        assertNotNull(updated.getReadAt());
        verify(notificationRepository, times(1)).save(notif);
    }

    @Test
    @DisplayName("dismiss() marks notification as dismissed")
    public void testDismiss_Success() {
        Notification notif = new Notification();
        notif.setId(501L);
        notif.setUser(userA);
        notif.setOrganizationId(ORG_ID);
        notif.setDismissed(false);

        when(notificationRepository.findByIdAndUserIdAndOrganizationId(501L, 101L, ORG_ID))
                .thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification dismissed = notificationService.dismiss(501L, 101L, ORG_ID);

        assertTrue(dismissed.isDismissed());
        assertNotNull(dismissed.getDismissedAt());
        verify(notificationRepository, times(1)).save(notif);
    }

    @Test
    @DisplayName("getUnreadCounts() returns unread and action required counts")
    public void testGetUnreadCounts() {
        when(notificationRepository.countByUserIdAndOrganizationIdAndIsReadFalseAndDismissedFalse(101L, ORG_ID))
                .thenReturn(8L);
        when(notificationRepository.countByUserIdAndOrganizationIdAndActionRequiredTrueAndIsReadFalseAndDismissedFalse(101L, ORG_ID))
                .thenReturn(3L);

        Map<String, Long> counts = notificationService.getUnreadCounts(101L, ORG_ID);

        assertEquals(8L, counts.get("unreadCount"));
        assertEquals(3L, counts.get("actionRequiredCount"));
    }
}
