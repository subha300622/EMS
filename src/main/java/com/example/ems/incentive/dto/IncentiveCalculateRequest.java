package com.example.ems.incentive.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IncentiveCalculateRequest {

    @NotNull(message = "Employee ID is mandatory.")
    private Long employeeId;

    private Long policyId;

    @NotNull(message = "Period start date is mandatory.")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is mandatory.")
    private LocalDate periodEnd;

    private BigDecimal targetValue;

    private BigDecimal achievedValue;

    private BigDecimal performanceRating;

    public IncentiveCalculateRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public BigDecimal getTargetAmount() { return targetValue; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetValue = targetAmount; }

    public BigDecimal getAchievedValue() { return achievedValue; }
    public void setAchievedValue(BigDecimal achievedValue) { this.achievedValue = achievedValue; }

    public BigDecimal getAchievedAmount() { return achievedValue; }
    public void setAchievedAmount(BigDecimal achievedAmount) { this.achievedValue = achievedAmount; }

    public BigDecimal getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(BigDecimal performanceRating) { this.performanceRating = performanceRating; }
}
