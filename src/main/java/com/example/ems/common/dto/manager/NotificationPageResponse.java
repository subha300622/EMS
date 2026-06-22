package com.example.ems.common.dto.manager;

import java.util.List;

public record NotificationPageResponse(
    NotificationStatsDto stats,
    long unreadCount,
    List<NotificationDto> notifications,
    List<AnnouncementDto> announcements,
    NotificationPreferenceDto preferences
) {}
