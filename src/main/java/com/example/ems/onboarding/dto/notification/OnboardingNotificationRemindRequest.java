package com.example.ems.onboarding.dto.notification;

import jakarta.validation.constraints.NotNull;

public class OnboardingNotificationRemindRequest {

    @NotNull(message = "taskId is required")
    private Long taskId;

    private String channel = "EMAIL";

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
