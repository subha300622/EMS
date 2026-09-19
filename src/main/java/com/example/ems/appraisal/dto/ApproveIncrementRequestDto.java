package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
