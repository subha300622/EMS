package com.example.ems.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class OnboardingRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private LocalDate startDate;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
