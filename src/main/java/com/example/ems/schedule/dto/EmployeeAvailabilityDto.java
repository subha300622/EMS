package com.example.ems.schedule.dto;

import java.time.LocalDate;

public class EmployeeAvailabilityDto {
    private String employeeId;
    private LocalDate date;
    private boolean available;
    private String reason; // "LEAVE", "NONE"
    private Long leaveRequestId;

    public EmployeeAvailabilityDto() {}

    public EmployeeAvailabilityDto(String employeeId, LocalDate date, boolean available, String reason, Long leaveRequestId) {
        this.employeeId = employeeId;
        this.date = date;
        this.available = available;
        this.reason = reason;
        this.leaveRequestId = leaveRequestId;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
}
