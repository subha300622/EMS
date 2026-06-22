package com.example.ems.common.dto.manager;

public record NotificationPreferenceDto(
    Boolean emailNotifications,
    Boolean pushNotifications,
    Boolean approvalAlerts,
    Boolean systemAlerts,
    Boolean announcementAlerts
) {}
