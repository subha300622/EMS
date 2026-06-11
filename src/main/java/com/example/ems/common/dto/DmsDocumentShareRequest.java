package com.example.ems.common.dto;

import com.example.ems.employee.entity.Employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DmsDocumentShareRequest {

    @NotNull(message = "Employee ID to share with is required")
    private Long employeeId;

    @NotBlank(message = "Access level is required")
    private String accessLevel = "READ"; // READ, WRITE

    public DmsDocumentShareRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
