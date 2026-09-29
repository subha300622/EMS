package com.example.ems.onboarding.event;

public class OnboardingNotificationEvent {

    private Long onboardingId;
    private String eventType; // TASK_REMINDER, WELCOME_RESEND
    private Long taskId;
    private String channel;
    private String recipientEmail;

    public OnboardingNotificationEvent(Long onboardingId, String eventType, Long taskId, String channel, String recipientEmail) {
        this.onboardingId = onboardingId;
        this.eventType = eventType;
        this.taskId = taskId;
        this.channel = channel;
        this.recipientEmail = recipientEmail;
    }

    public Long getOnboardingId() { return onboardingId; }
    public String getEventType() { return eventType; }
    public Long getTaskId() { return taskId; }
    public String getChannel() { return channel; }
    public String getRecipientEmail() { return recipientEmail; }
}
