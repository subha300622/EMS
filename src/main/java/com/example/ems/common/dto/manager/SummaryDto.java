package com.example.ems.common.dto.manager;

public record SummaryDto(
    Long teamSize,
    Double attendanceRate,
    Long onLeaveToday,
    Long teamOvertimeHours,
    Long activeCount,
    Long wfhCount
) {}
