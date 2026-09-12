package com.example.ems.increment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDate;

public class UpdateRecommendationRequest {

    @DecimalMin(value = "0.0", message = "Increment percentage must be >= 0")
    @DecimalMax(value = "100.0", message = "Increment percentage must be <= 100")
    private Double incrementPercentage;

    private LocalDate effectiveDate;

    private String comments;

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
