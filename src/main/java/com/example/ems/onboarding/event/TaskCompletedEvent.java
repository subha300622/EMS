package com.example.ems.onboarding.event;

import org.springframework.context.ApplicationEvent;

public class TaskCompletedEvent extends ApplicationEvent {
    private final Long onboardingId;
    private final Long taskId;
    private final String phaseName;

    public TaskCompletedEvent(Object source, Long onboardingId, Long taskId, String phaseName) {
        super(source);
        this.onboardingId = onboardingId;
        this.taskId = taskId;
        this.phaseName = phaseName;
    }

    public Long getOnboardingId() {
        return onboardingId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getPhaseName() {
        return phaseName;
    }
}
