package com.example.ems.leave.event;

import java.time.Instant;

public record LeaveRejectedEvent(
        Long leaveId,
        String employeeCode,
        Long organizationId,
        String rejectedBy,
        String reason,
        Instant timestamp
) {
    public LeaveRejectedEvent(Long leaveId, String employeeCode, Long organizationId, String rejectedBy, String reason) {
        this(leaveId, employeeCode, organizationId, rejectedBy, reason, Instant.now());
    }
}
