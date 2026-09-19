package com.example.ems.onboarding.dto.task;

import jakarta.validation.constraints.NotBlank;

public class TaskAssignRequest {

    @NotBlank(message = "employeeId is required")
    private String employeeId;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}
