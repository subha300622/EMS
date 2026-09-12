package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalIncrementResponseDto {
    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private BigDecimal currentSalary;
    private Double incrementPercentage;
    private BigDecimal incrementAmount;
    private Double bonusPercentage;
    private BigDecimal bonusAmount;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String status; // PROPOSED, APPROVED, APPLIED, REJECTED
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String appliedByName;
    private LocalDateTime appliedAt;
    private String remarks;
    private LocalDateTime createdAt;
}
