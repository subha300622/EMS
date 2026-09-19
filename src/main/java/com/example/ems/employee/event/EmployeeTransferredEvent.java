package com.example.ems.employee.event;

import java.time.Instant;

public record EmployeeTransferredEvent(
    Long employeeId,
    Long organizationId,
    Long fromDepartmentId,
    Long toDepartmentId,
    Instant occurredAt
) {}
