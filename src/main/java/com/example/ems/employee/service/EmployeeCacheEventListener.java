package com.example.ems.employee.service;

import com.example.ems.employee.event.EmployeeCreatedEvent;
import com.example.ems.employee.event.EmployeeUpdatedEvent;
import com.example.ems.employee.event.EmployeeDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that handles cache eviction asynchronously after transaction commit.
 */
@Component
public class EmployeeCacheEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCacheEventListener.class);

    @Autowired
    private EmployeeCacheService employeeCacheService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("[Event] Asynchronously handling EmployeeCreatedEvent for employee: {}", event.getEmployee().getId());
        employeeCacheService.evictAllRelatedCaches(event.getEmployee());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.info("[Event] Asynchronously handling EmployeeUpdatedEvent for employee: {}", event.getEmployee().getId());
        employeeCacheService.evictAllRelatedCaches(event.getEmployee());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeDeleted(EmployeeDeletedEvent event) {
        log.info("[Event] Asynchronously handling EmployeeDeletedEvent for employee: {}", event.getEmployee().getId());
        employeeCacheService.evictAllRelatedCaches(event.getEmployee());
    }
}
