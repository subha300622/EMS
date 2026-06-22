package com.example.ems.common.dto.manager;

public record LeaveSummaryDto(
    Long employeeId,
    String name,
    String leaveType,
    String startDate,
    String endDate,
    ApprovalStatus status
) {}
