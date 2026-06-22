package com.example.ems.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRegularizationRequest {

    private Long employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalTime proposedPunchInTime;

    private LocalTime proposedPunchOutTime;

    @NotNull(message = "Reason is required")
    private String reason;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getProposedPunchInTime() {
        return proposedPunchInTime;
    }

    public void setProposedPunchInTime(LocalTime proposedPunchInTime) {
        this.proposedPunchInTime = proposedPunchInTime;
    }

    public LocalTime getProposedPunchOutTime() {
        return proposedPunchOutTime;
    }

    public void setProposedPunchOutTime(LocalTime proposedPunchOutTime) {
        this.proposedPunchOutTime = proposedPunchOutTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
