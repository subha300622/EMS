package com.example.ems.dto;

import jakarta.validation.constraints.NotNull;

public class DmsSignatureRequest {

    @NotNull(message = "Employee ID requested to sign is required")
    private Long employeeId;

    private String comments;

    public DmsSignatureRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
