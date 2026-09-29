package com.example.ems.increment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateRecommendationRequest {

    @NotNull(message = "Cycle ID is required")
    private Long cycleId;

    private Long appraisalId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @DecimalMin(value = "0.0", message = "Increment percentage must be >= 0")
    @DecimalMax(value = "100.0", message = "Increment percentage must be <= 100")
    private Double incrementPercentage;

    private LocalDate effectiveDate;

    private String comments;

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
