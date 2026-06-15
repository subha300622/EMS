package com.example.ems.training.dto;

import jakarta.validation.constraints.NotNull;

public class TrainingEnrollmentRequest {

    @NotNull(message = "Employee ID is required")
    private String employeeId;

    @NotNull(message = "Session ID is required")
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
