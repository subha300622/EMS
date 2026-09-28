package com.example.ems.common.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalActionRequiredEvent;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.notification.NotificationCommand;
import com.example.ems.common.dto.notification.NotificationRecipientContext;
import com.example.ems.common.entity.*;
import com.example.ems.common.repository.NotificationDeliveryRepository;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDeliveryRepository deliveryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipientResolver recipientResolver;

    // ── Target Core Notification Service Methods ───────────────────────────────

    @Transactional
    public Notification create(NotificationCommand command) {
        if (command == null || command.getRecipientUserId() == null) {
            throw new IllegalArgumentException("Notification command and recipientUserId are required");
        }

        Long orgId = command.getOrganizationId();
        if (orgId == null) {
            orgId = TenantContext.getOrganizationId();
        }

        User recipient = recipientResolver.resolveExplicitUser(command.getRecipientUserId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Active recipient user not found for ID: " + command.getRecipientUserId()));

        if (orgId == null && recipient.getOrganizationId() != null) {
            orgId = recipient.getOrganizationId();
        }

        // Deterministic idempotency check
        if (command.getIdempotencyKey() != null && !command.getIdempotencyKey().isBlank()) {
            Optional<Notification> existing = notificationRepository.findByIdempotencyKey(command.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Notification with idempotency key '{}' already exists: safe return", command.getIdempotencyKey());
                return existing.get();
            }
        }

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setOrganizationId(orgId);
        notification.setTitle(command.getTitle());
        notification.setMessage(command.getMessage());

        if (command.getCategory() != null) {
            notification.setCategory(command.getCategory());
        } else {
            notification.setCategory(NotificationCategory.SYSTEM);
        }

        if (command.getType() != null) {
            notification.setNotificationType(command.getType());
        } else if (command.getCustomType() != null && !command.getCustomType().isBlank()) {
            notification.setType(command.getCustomType());
        } else {
            notification.setType("SYSTEM");
        }

        if (command.getPriority() != null) {
            notification.setNotificationPriority(command.getPriority());
        } else {
            notification.setPriority("MEDIUM");
        }

        notification.setActionRequired(command.isActionRequired());
        notification.setActionUrl(command.getActionUrl());
        notification.setEntityType(command.getEntityType());
        notification.setEntityId(command.getEntityId());
        notification.setMetadata(command.getMetadata());
        notification.setIdempotencyKey(command.getIdempotencyKey());
        notification.setExpiresAt(command.getExpiresAt());
        notification.setRead(false);
        notification.setDismissed(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        // Record initial in-app delivery entry
        NotificationDelivery delivery = new NotificationDelivery(
                saved, recipient, DeliveryChannel.IN_APP, DeliveryStatus.SENT);
        deliveryRepository.save(delivery);

        return saved;
    }

    @Transactional
    public List<Notification> createForContext(NotificationRecipientContext recipientContext, NotificationCommand baseCommand) {
        Set<User> recipients = recipientResolver.resolveRecipients(recipientContext);
        if (recipients.isEmpty()) {
            log.warn("No recipients resolved for notification context: {}", recipientContext);
            return Collections.emptyList();
        }

        List<Notification> created = new ArrayList<>();
        for (User recipient : recipients) {
            String scopedKey = baseCommand.getIdempotencyKey();
            if (scopedKey != null && !scopedKey.isBlank()) {
                scopedKey = scopedKey + ":" + recipient.getId();
            }

            NotificationCommand command = NotificationCommand.builder()
                    .organizationId(recipientContext.getOrganizationId() != null ? recipientContext.getOrganizationId() : recipient.getOrganizationId())
                    .recipientUserId(recipient.getId())
                    .category(baseCommand.getCategory())
                    .type(baseCommand.getType())
                    .customType(baseCommand.getCustomType())
                    .title(baseCommand.getTitle())
                    .message(baseCommand.getMessage())
                    .priority(baseCommand.getPriority())
                    .actionRequired(baseCommand.isActionRequired())
                    .actionUrl(baseCommand.getActionUrl())
                    .entityType(baseCommand.getEntityType())
                    .entityId(baseCommand.getEntityId())
                    .metadata(baseCommand.getMetadata())
                    .idempotencyKey(scopedKey)
                    .expiresAt(baseCommand.getExpiresAt())
                    .build();

            created.add(create(command));
        }
        return created;
    }

    @Transactional
    public Notification createForUser(Long userId, NotificationCommand command) {
        command.setRecipientUserId(userId);
        return create(command);
    }

    @Transactional
    public List<Notification> createForAssignment(String entityType, String entityId, Long assigneeUserId, NotificationCommand command) {
        NotificationRecipientContext context = NotificationRecipientContext.builder()
                .organizationId(command.getOrganizationId())
                .entityType(entityType)
                .entityId(entityId)
                .assigneeUserId(assigneeUserId)
                .build();
        return createForContext(context, command);
    }

    @Transactional
    public Notification markRead(Long notificationId, Long userId, Long organizationId) {
        Notification notification = notificationRepository.findByIdAndUserIdAndOrganizationId(notificationId, userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied for ID: " + notificationId));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllRead(Long userId, Long organizationId, String categoryName) {
        if (categoryName != null && !categoryName.isBlank() && !"ALL".equalsIgnoreCase(categoryName)) {
            try {
                NotificationCategory category = NotificationCategory.valueOf(categoryName.toUpperCase());
                return notificationRepository.markAllAsReadForUserAndOrgByCategory(userId, organizationId, category);
            } catch (IllegalArgumentException ignored) {}
        }
        return notificationRepository.markAllAsReadForUserAndOrg(userId, organizationId);
    }

    @Transactional
    public Notification dismiss(Long notificationId, Long userId, Long organizationId) {
        Notification notification = notificationRepository.findByIdAndUserIdAndOrganizationId(notificationId, userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied for ID: " + notificationId));
        notification.setDismissed(true);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getInbox(Long userId, Long organizationId, Pageable pageable) {
        return notificationRepository.findByUserIdAndOrganizationIdAndDismissedFalseOrderByCreatedAtDesc(userId, organizationId, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadCounts(Long userId, Long organizationId) {
        long unread = notificationRepository.countByUserIdAndOrganizationIdAndIsReadFalseAndDismissedFalse(userId, organizationId);
        long actionRequired = notificationRepository.countByUserIdAndOrganizationIdAndActionRequiredTrueAndIsReadFalseAndDismissedFalse(userId, organizationId);
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", unread);
        result.put("actionRequiredCount", actionRequired);
        return result;
    }

    // ── Existing Backward-Compatible Methods ───────────────────────────────────

    public Notification sendNotification(User user, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        if (user != null && user.getOrganizationId() != null) {
            n.setOrganizationId(user.getOrganizationId());
        }
        return notificationRepository.save(n);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.setRead(true);
        return notificationRepository.save(n);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    public boolean deleteNotification(Long id, Long userId) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent() && opt.get().getUser().getId().equals(userId)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification createApprovalNotification(ApprovalActionRequiredEvent event) {
        if (event == null || event.getApproverEmployeeId() == null) {
            return null;
        }

        try {
            User approverUser = recipientResolver.resolveEmployeeUser(
                    event.getApproverEmployeeId(), event.getOrganizationId()).orElse(null);

            if (approverUser == null) {
                Employee approverEmp = employeeRepository.findById(event.getApproverEmployeeId()).orElse(null);
                if (approverEmp != null && approverEmp.getEmail() != null) {
                    approverUser = userRepository.findByWorkEmail(approverEmp.getEmail()).orElse(null);
                }
            }

            if (approverUser == null) {
                log.warn("Cannot notify: No active User account found for approver employee #{}", event.getApproverEmployeeId());
                return null;
            }

            String workflowName = formatWorkflowTitle(event.getWorkflowType());
            String stageName = event.getStageName() != null ? event.getStageName() : "Stage " + event.getStageOrder();
            String title = workflowName + " Approval Required";
            String message = String.format("Request #%s requires your approval at stage '%s'.",
                    event.getBusinessReferenceId(), stageName);

            String idempotencyKey = String.format("APPROVAL:%s:%d:%d",
                    event.getApprovalTaskId(),
                    event.getStageOrder() != null ? event.getStageOrder() : 1,
                    approverUser.getId());

            // 1. In-memory / DB pre-check for quick short-circuit
            if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
                log.info("Approval notification already exists for idempotencyKey {}: safe no-op", idempotencyKey);
                return notificationRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            }

            // 2. Atomic INSERT ... ON CONFLICT (idempotency_key) DO NOTHING
            LocalDateTime now = LocalDateTime.now();
            int rowsAffected = notificationRepository.insertNotificationIfNotExists(
                    approverUser.getId(),
                    title,
                    message,
                    "APPROVAL",
                    "HIGH",
                    false,
                    now,
                    idempotencyKey
            );

            Notification saved = notificationRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (saved != null) {
                // Ensure new schema fields (category, organization, action_required) are populated
                if (saved.getCategory() == null || saved.getCategory() == NotificationCategory.SYSTEM) {
                    saved.setCategory(NotificationCategory.APPROVAL);
                }
                if (saved.getOrganizationId() == null && event.getOrganizationId() != null) {
                    saved.setOrganizationId(event.getOrganizationId());
                }
                saved.setActionRequired(true);
                saved.setEntityType(event.getBusinessReferenceType());
                notificationRepository.save(saved);
            }

            if (rowsAffected > 0) {
                log.info("Created approval in-app notification for user {} (task: {}, key: {})",
                        approverUser.getWorkEmail(), event.getApprovalTaskId(), idempotencyKey);
            } else {
                log.info("Concurrent duplicate approval notification prevented by ON CONFLICT for key {}: safe no-op", idempotencyKey);
            }

            return saved;
        } catch (Exception e) {
            log.error("Failed to create approval notification for task {}: {}",
                    event.getApprovalTaskId(), e.getMessage(), e);
            return null;
        }
    }

    private String formatWorkflowTitle(WorkflowType type) {
        if (type == null) return "Approval";
        return switch (type) {
            case LEAVE_APPROVAL -> "Leave Request";
            case FNF_APPROVAL -> "F&F Settlement";
            case PERFORMANCE_REVIEW -> "Performance Review";
            case EXPENSE_APPROVAL -> "Expense Claim";
            case EMPLOYEE_EXIT -> "Employee Exit";
            case APPRAISAL_REVIEW -> "Appraisal Review";
            case PAYROLL_APPROVAL -> "Payroll";
            default -> "Approval Task";
        };
    }
}
