package com.example.ems.common.dto.manager;

public record OvertimeDto(
    Long employeeId,
    String name,
    Double overtimeHours,
    Double thresholdHours,
    String status
) {}
