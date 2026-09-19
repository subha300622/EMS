package com.example.ems.common.service;

import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.event.ApprovalActionRequiredEvent;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.entity.Notification;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification sendNotification(User user, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
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
            Employee approverEmp = employeeRepository.findById(event.getApproverEmployeeId()).orElse(null);
            if (approverEmp == null || approverEmp.getEmail() == null) {
                log.warn("Cannot notify: Approver employee #{} not found or has no email", event.getApproverEmployeeId());
                return null;
            }

            User approverUser = null;
            if (event.getOrganizationId() != null) {
                approverUser = userRepository.findByWorkEmailAndOrganizationId(approverEmp.getEmail(), event.getOrganizationId())
                        .orElse(null);
            }
            if (approverUser == null && approverEmp.getOrganization() != null) {
                approverUser = userRepository.findByWorkEmailAndOrganizationId(approverEmp.getEmail(), approverEmp.getOrganization().getId())
                        .orElse(null);
            }
            if (approverUser == null) {
                approverUser = userRepository.findByWorkEmail(approverEmp.getEmail()).orElse(null);
            }

            if (approverUser == null) {
                log.warn("Cannot notify: No User account for work email {}", approverEmp.getEmail());
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

            if (rowsAffected > 0) {
                log.info("Created approval in-app notification for user {} (task: {}, key: {})",
                        approverUser.getWorkEmail(), event.getApprovalTaskId(), idempotencyKey);
            } else {
                log.info("Concurrent duplicate approval notification prevented by ON CONFLICT for key {}: safe no-op", idempotencyKey);
            }

            return notificationRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
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
