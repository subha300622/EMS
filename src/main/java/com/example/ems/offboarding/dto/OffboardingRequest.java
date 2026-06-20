package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class OffboardingRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @NotBlank(message = "Reason for offboarding is required")
    @Schema(example = "Personal business")
    private String reason;

    @Schema(example = "2026-06-19")
    private LocalDate exitDate;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }
}
