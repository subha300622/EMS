package com.example.ems.reminder.dto;

import java.io.Serializable;
import java.util.List;

public class ReminderListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ReminderResponse> reminders;

    public ReminderListResponse() {}

    public ReminderListResponse(List<ReminderResponse> reminders) {
        this.reminders = reminders;
    }

    public List<ReminderResponse> getReminders() {
        return reminders;
    }

    public void setReminders(List<ReminderResponse> reminders) {
        this.reminders = reminders;
    }
}
