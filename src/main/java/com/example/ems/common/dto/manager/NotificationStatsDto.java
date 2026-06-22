package com.example.ems.common.dto.manager;

public record NotificationStatsDto(
    long total,
    long unread,
    long approvals,
    long mentions,
    long system
) {}
