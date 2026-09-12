package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncrementCalculationPreviewDto {
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private Double finalRating;
    private String performanceCategory;
    private BigDecimal currentSalary;
    private Double suggestedIncrementPercentage;
    private Double bonusPercentage;
    private BigDecimal incrementAmount;
    private BigDecimal bonusAmount;
    private BigDecimal proposedSalary;
    private boolean isEligible;
    private String policyName;
    private LocalDate effectiveDate;
}
