package com.example.ems.leave.dto;

import jakarta.validation.constraints.NotNull;

public class LeaveActionRequest {

    @NotNull(message = "Leave ID is required")
    private Long leaveId;

    public LeaveActionRequest() {}

    public LeaveActionRequest(Long leaveId) {
        this.leaveId = leaveId;
    }

    public Long getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Long leaveId) {
        this.leaveId = leaveId;
    }
}
