package com.example.ems.common.dto.manager;

public record NotificationDto(
    Long id,
    String title,
    String message,
    String type,
    String priority,
    boolean read,
    String createdAt
) {}
