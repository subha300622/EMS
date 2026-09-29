package com.example.ems.bonus.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BonusCalculateRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private Long policyId;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    private BigDecimal achievementValue;

    private BigDecimal performanceRating;

    private BigDecimal discretionaryAmount;

    public BonusCalculateRequest() {}

    public BonusCalculateRequest(Long employeeId, Long policyId, LocalDate periodStart, LocalDate periodEnd) {
        this.employeeId = employeeId;
        this.policyId = policyId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getAchievementValue() {
        return achievementValue;
    }

    public void setAchievementValue(BigDecimal achievementValue) {
        this.achievementValue = achievementValue;
    }

    public BigDecimal getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(BigDecimal performanceRating) {
        this.performanceRating = performanceRating;
    }

    public BigDecimal getDiscretionaryAmount() {
        return discretionaryAmount;
    }

    public void setDiscretionaryAmount(BigDecimal discretionaryAmount) {
        this.discretionaryAmount = discretionaryAmount;
    }
}
