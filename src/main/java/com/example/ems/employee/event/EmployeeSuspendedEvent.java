package com.example.ems.employee.event;

import java.time.Instant;

public record EmployeeSuspendedEvent(
    Long employeeId,
    Long organizationId,
    String reason,
    Instant occurredAt
) {}
