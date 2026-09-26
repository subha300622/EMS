package com.example.ems.employee.listener;

import com.example.ems.employee.event.*;
import com.example.ems.employee.service.EmployeeLifecycleAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that listens for Employee lifecycle events AFTER_COMMIT
 * and records compliance audit records in {@link com.example.ems.organization.entity.OrganizationAuditLog}
 * via {@link EmployeeLifecycleAuditService}.
 */
@Component
public class EmployeeLifecycleAuditListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLifecycleAuditListener.class);

    private final EmployeeLifecycleAuditService auditService;

    public EmployeeLifecycleAuditListener(EmployeeLifecycleAuditService auditService) {
        this.auditService = auditService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeJoinedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        auditService.auditEmployeeJoined(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeActivatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        auditService.auditEmployeeActivated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeTransferredEvent AFTER_COMMIT for employee: {}, fromDept: {}, toDept: {}",
                event.employeeId(), event.fromDepartmentId(), event.toDepartmentId());
        auditService.auditEmployeeTransferred(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeManagerChangedEvent AFTER_COMMIT for employee: {}, oldManager: {}, newManager: {}",
                event.employeeId(), event.oldManagerId(), event.newManagerId());
        auditService.auditEmployeeManagerChanged(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeSuspendedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        auditService.auditEmployeeSuspended(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleAuditListener] Handling EmployeeTerminatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        auditService.auditEmployeeTerminated(event);
    }
}
