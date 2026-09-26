package com.example.ems.onboarding.service;

import com.example.ems.onboarding.dto.notification.OnboardingNotificationRemindRequest;
import com.example.ems.onboarding.dto.notification.OnboardingNotificationResendRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.event.OnboardingNotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OnboardingNotificationService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingAuditLogService auditLogService;

    public List<Map<String, Object>> getNotifications(Long onboardingId) {
        securityValidator.validateAndGetOnboarding(onboardingId);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of(
                "id", 1L,
                "type", "ONBOARDING_WELCOME",
                "channel", "EMAIL",
                "status", "SENT",
                "sentAt", "2026-08-24T09:00:00"
        ));
        return list;
    }

    public void sendReminder(Long onboardingId, OnboardingNotificationRemindRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);

        String email = onboarding.getEmployee().getEmail();
        OnboardingNotificationEvent event = new OnboardingNotificationEvent(
                onboardingId, "TASK_REMINDER", request.getTaskId(), request.getChannel(), email);

        eventPublisher.publishEvent(event);
        auditLogService.logAction(onboarding, "NOTIFICATION_REMINDER_SENT", "ONBOARDING_TASK", request.getTaskId(),
                "Sent reminder via " + request.getChannel());
    }

    public void resendNotification(Long onboardingId, OnboardingNotificationResendRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);

        String email = onboarding.getEmployee().getEmail();
        OnboardingNotificationEvent event = new OnboardingNotificationEvent(
                onboardingId, request.getNotificationType(), null, request.getChannel(), email);

        eventPublisher.publishEvent(event);
        auditLogService.logAction(onboarding, "NOTIFICATION_RESENT", "ONBOARDING", onboardingId,
                "Resent notification " + request.getNotificationType() + " via " + request.getChannel());
    }
}
