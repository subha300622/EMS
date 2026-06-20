package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TrainingAttendanceRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "string")
    private String employeeId;

    @NotNull(message = "Attendance date is required")
    @Schema(example = "2026-06-19")
    private LocalDate attendanceDate;

    @NotBlank(message = "Attendance status is required")
    @Schema(example = "ACTIVE")
    private String status; // PRESENT, ABSENT, LATE

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
