package com.example.ems.employee.event;

import java.time.Instant;

public record EmployeeManagerChangedEvent(
    Long employeeId,
    Long organizationId,
    Long oldManagerId,
    Long newManagerId,
    Instant occurredAt
) {}
