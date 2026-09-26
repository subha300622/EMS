package com.example.ems.employee.event;

import java.time.Instant;

public record EmployeeJoinedEvent(
    Long employeeId,
    Long organizationId,
    Instant occurredAt
) {}
