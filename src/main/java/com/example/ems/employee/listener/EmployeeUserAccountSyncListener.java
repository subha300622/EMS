package com.example.ems.employee.listener;

import com.example.ems.employee.event.*;
import com.example.ems.employee.service.EmployeeUserAccountSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that listens for Employee lifecycle events AFTER_COMMIT
 * and delegates User authentication account synchronization to {@link EmployeeUserAccountSyncService}.
 */
@Component
public class EmployeeUserAccountSyncListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeUserAccountSyncListener.class);

    private final EmployeeUserAccountSyncService syncService;

    public EmployeeUserAccountSyncListener(EmployeeUserAccountSyncService syncService) {
        this.syncService = syncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeJoinedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        syncService.syncOnEmployeeJoined(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeActivatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        syncService.syncOnEmployeeActivated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeSuspendedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        syncService.syncOnEmployeeSuspended(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeTerminatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        syncService.syncOnEmployeeTerminated(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeTransferredEvent AFTER_COMMIT for employee: {}, toDept: {}",
                event.employeeId(), event.toDepartmentId());
        syncService.syncOnEmployeeTransferred(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSyncListener] Handling EmployeeManagerChangedEvent AFTER_COMMIT for employee: {}, newManager: {}",
                event.employeeId(), event.newManagerId());
        syncService.syncOnEmployeeManagerChanged(event);
    }
}
