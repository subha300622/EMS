package com.example.ems.onboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class OnboardingRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(example = "1")
    private Long employeeId;

    @Schema(example = "2026-06-19")
    private LocalDate startDate;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
