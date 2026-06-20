package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

public class TrainingEnrollmentRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "string")
    private String employeeId;

    @NotNull(message = "Session ID is required")
    @Schema(example = "1")
    private Long sessionId;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
