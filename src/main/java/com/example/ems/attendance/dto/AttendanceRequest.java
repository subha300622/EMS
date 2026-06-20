package com.example.ems.attendance.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotNull(message = "Date is required")
    @Schema(example = "2026-06-19")
    private LocalDate date;

    @NotBlank(message = "Status is required")
    @Schema(example = "ACTIVE")
    private String status;

    private LocalTime punchInTime;

    private LocalTime punchOutTime;

    @Schema(example = "string")
    private String notes;

    public AttendanceRequest() {}

    public AttendanceRequest(Long employeeId, LocalDate date, String status, LocalTime punchInTime, LocalTime punchOutTime, String notes) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
        this.punchInTime = punchInTime;
        this.punchOutTime = punchOutTime;
        this.notes = notes;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalTime getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(LocalTime punchInTime) {
        this.punchInTime = punchInTime;
    }

    public LocalTime getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(LocalTime punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
