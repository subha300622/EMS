package com.example.ems.maintenance.event;

import java.time.OffsetDateTime;

public record MaintenanceScheduleStartedEvent(
        String message,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {}
