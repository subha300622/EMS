package com.example.ems.reminder.event;

import com.example.ems.reminder.entity.Reminder;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a reminder is created.
 */
public class ReminderCreatedEvent extends ApplicationEvent {
    private final Reminder reminder;

    public ReminderCreatedEvent(Object source, Reminder reminder) {
        super(source);
        this.reminder = reminder;
    }

    public Reminder getReminder() {
        return reminder;
    }
}
