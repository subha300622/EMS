package com.example.ems.employee.event;

import java.time.Instant;

public record EmployeeActivatedEvent(
    Long employeeId,
    Long organizationId,
    Instant occurredAt
) {}
