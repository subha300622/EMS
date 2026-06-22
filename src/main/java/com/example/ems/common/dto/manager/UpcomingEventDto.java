package com.example.ems.common.dto.manager;

public record UpcomingEventDto(
    String title,
    String eventDate,
    String type,
    String description
) {}
