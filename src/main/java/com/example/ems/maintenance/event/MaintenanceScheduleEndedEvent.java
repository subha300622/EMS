package com.example.ems.maintenance.event;

import java.time.OffsetDateTime;

public record MaintenanceScheduleEndedEvent(
        String message,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {}
