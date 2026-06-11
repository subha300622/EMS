package com.example.ems.appraisal.dto;

import com.example.ems.employee.entity.Employee;

import jakarta.validation.constraints.NotNull;

public class AppraisalRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Cycle ID is required")
    private Long cycleId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }
}
