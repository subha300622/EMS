package com.example.ems.reminder.service;

import com.example.ems.reminder.dto.ReminderResponse;
import com.example.ems.reminder.entity.Reminder;
import com.example.ems.reminder.event.ReminderCreatedEvent;
import com.example.ems.reminder.event.ReminderUpdatedEvent;
import com.example.ems.reminder.event.ReminderDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that handles reminder cache evictions asynchronously after commit.
 */
@Component
public class ReminderCacheEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReminderCacheEventListener.class);

    @Autowired
    private ReminderCacheService reminderCacheService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReminderCreated(ReminderCreatedEvent event) {
        log.info("[Event] Asynchronously handling ReminderCreatedEvent for: {}", event.getReminder().getId());
        reminderCacheService.onCreated(toResponse(event.getReminder()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReminderUpdated(ReminderUpdatedEvent event) {
        log.info("[Event] Asynchronously handling ReminderUpdatedEvent for: {}", event.getReminder().getId());
        reminderCacheService.onUpdated(toResponse(event.getReminder()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReminderDeleted(ReminderDeletedEvent event) {
        Reminder reminder = event.getReminder();
        log.info("[Event] Asynchronously handling ReminderDeletedEvent for: {}", reminder.getId());
        reminderCacheService.onDeleted(reminder.getId(), reminder.getEmployeeId());
    }

    private ReminderResponse toResponse(Reminder r) {
        ReminderResponse res = new ReminderResponse();
        res.setId(r.getId());
        res.setTitle(r.getTitle());
        res.setDescription(r.getDescription());
        res.setReminderDate(r.getReminderDate());
        res.setEmployeeId(r.getEmployeeId());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        return res;
    }
}
