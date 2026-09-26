package com.example.ems.maintenance.event;

import java.time.OffsetDateTime;

public record MaintenanceEnabledEvent(
        boolean allowAdminAccess,
        boolean logoutActiveSessions,
        String message,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String performedBy
) {}
