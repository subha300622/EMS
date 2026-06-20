package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DmsDocumentShareRequest {

    @NotNull(message = "Employee ID to share with is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotBlank(message = "Access level is required")
    @Schema(example = "string")
    private String accessLevel = "READ"; // READ, WRITE

    public DmsDocumentShareRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}
