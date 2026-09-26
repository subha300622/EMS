package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ApproveIncrementRequestDto {

    @NotNull(message = "Approved increment percentage is required")
    @DecimalMin(value = "0.0", message = "Increment percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Increment percentage cannot exceed 100%")
    private Double approvedIncrementPercentage;

    @DecimalMin(value = "0.0", message = "Bonus percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Bonus percentage cannot exceed 100%")
    private Double bonusPercentage;

    private LocalDate effectiveDate;

    private String remarks;

    public ApproveIncrementRequestDto() {}

    public ApproveIncrementRequestDto(Double approvedIncrementPercentage, Double bonusPercentage,
                                    LocalDate effectiveDate, String remarks) {
        this.approvedIncrementPercentage = approvedIncrementPercentage;
        this.bonusPercentage = bonusPercentage;
        this.effectiveDate = effectiveDate;
        this.remarks = remarks;
    }

    public Double getApprovedIncrementPercentage() { return approvedIncrementPercentage; }
    public void setApprovedIncrementPercentage(Double approvedIncrementPercentage) { this.approvedIncrementPercentage = approvedIncrementPercentage; }

    public Double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(Double bonusPercentage) { this.bonusPercentage = bonusPercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
