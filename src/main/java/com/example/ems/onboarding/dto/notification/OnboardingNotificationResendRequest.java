package com.example.ems.onboarding.dto.notification;

public class OnboardingNotificationResendRequest {

    private String notificationType = "ONBOARDING_WELCOME";
    private String channel = "EMAIL";

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
