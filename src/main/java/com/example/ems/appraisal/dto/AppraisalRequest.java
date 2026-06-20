package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;

public class AppraisalRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotNull(message = "Cycle ID is required")
    @Schema(example = "1")
    private Long cycleId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }
}
