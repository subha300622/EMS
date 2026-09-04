package com.example.ems.leave.dto;

public class CreateEncashmentRequest {
    private Long leaveTypeId;
    private Double daysEncashed;
    private String reason;

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public Double getDaysEncashed() { return daysEncashed; }
    public void setDaysEncashed(Double daysEncashed) { this.daysEncashed = daysEncashed; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
