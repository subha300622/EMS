package com.example.ems.employee.listener;

import com.example.ems.employee.event.*;
import com.example.ems.employee.service.EmployeeLifecycleNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that listens for Employee lifecycle events AFTER_COMMIT
 * and dispatches communication-only lifecycle notifications via {@link EmployeeLifecycleNotificationService}.
 */
@Component
public class EmployeeLifecycleNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLifecycleNotificationListener.class);

    private final EmployeeLifecycleNotificationService notificationService;

    public EmployeeLifecycleNotificationListener(EmployeeLifecycleNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeJoinedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        notificationService.notifyEmployeeJoined(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeActivatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        notificationService.notifyEmployeeActivated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeTransferredEvent AFTER_COMMIT for employee: {}, fromDept: {}, toDept: {}",
                event.employeeId(), event.fromDepartmentId(), event.toDepartmentId());
        notificationService.notifyEmployeeTransferred(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeManagerChangedEvent AFTER_COMMIT for employee: {}, oldManager: {}, newManager: {}",
                event.employeeId(), event.oldManagerId(), event.newManagerId());
        notificationService.notifyEmployeeManagerChanged(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeSuspendedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        notificationService.notifyEmployeeSuspended(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleNotifListener] Handling EmployeeTerminatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        notificationService.notifyEmployeeTerminated(event);
    }
}
