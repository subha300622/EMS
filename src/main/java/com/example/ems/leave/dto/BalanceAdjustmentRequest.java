package com.example.ems.leave.dto;

public class BalanceAdjustmentRequest {
    private String employeeId; // e.g. "EMP-1001" or numeric string
    private Long leaveTypeId;
    private Double adjustment;
    private String reason;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Long getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Long leaveTypeId) { this.leaveTypeId = leaveTypeId; }

    public Double getAdjustment() { return adjustment; }
    public void setAdjustment(Double adjustment) { this.adjustment = adjustment; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
