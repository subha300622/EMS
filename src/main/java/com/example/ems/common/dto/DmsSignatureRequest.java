package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;

public class DmsSignatureRequest {

    @NotNull(message = "Employee ID requested to sign is required")
    @Schema(example = "1")
    private Long employeeId;

    @Schema(example = "Excellent progress")
    private String comments;

    public DmsSignatureRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
