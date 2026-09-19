package com.example.ems.leave.event;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveRequestedEvent(
        Long leaveId,
        String employeeCode,
        Long organizationId,
        LocalDate startDate,
        LocalDate endDate,
        Double durationDays,
        String leaveTypeName,
        Instant timestamp
) {
    public LeaveRequestedEvent(Long leaveId, String employeeCode, Long organizationId, LocalDate startDate, LocalDate endDate, Double durationDays, String leaveTypeName) {
        this(leaveId, employeeCode, organizationId, startDate, endDate, durationDays, leaveTypeName, Instant.now());
    }
}
