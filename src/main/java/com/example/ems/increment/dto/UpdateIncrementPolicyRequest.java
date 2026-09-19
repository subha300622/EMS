package com.example.ems.increment.dto;

import com.example.ems.increment.entity.EffectiveDateRule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UpdateIncrementPolicyRequest {

    private String name;
    private Boolean appraisalRequired;
    private Double minimumRating;
    private Double minimumGoalAchievementPercentage;
    private Double minimumAttendancePercentage;
    private Integer minimumServiceMonths;
    private Double maximumIncrementPercentage;
    private Double minimumIncrementPercentage;
    private EffectiveDateRule effectiveDateRule;
    private LocalDate effectiveDate;
    private BigDecimal budgetLimit;
    private Boolean active;
    private List<PolicyBandDto> bands;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getAppraisalRequired() { return appraisalRequired; }
    public void setAppraisalRequired(Boolean appraisalRequired) { this.appraisalRequired = appraisalRequired; }

    public Double getMinimumRating() { return minimumRating; }
    public void setMinimumRating(Double minimumRating) { this.minimumRating = minimumRating; }

    public Double getMinimumGoalAchievementPercentage() { return minimumGoalAchievementPercentage; }
    public void setMinimumGoalAchievementPercentage(Double minimumGoalAchievementPercentage) { this.minimumGoalAchievementPercentage = minimumGoalAchievementPercentage; }

    public Double getMinimumAttendancePercentage() { return minimumAttendancePercentage; }
    public void setMinimumAttendancePercentage(Double minimumAttendancePercentage) { this.minimumAttendancePercentage = minimumAttendancePercentage; }

    public Integer getMinimumServiceMonths() { return minimumServiceMonths; }
    public void setMinimumServiceMonths(Integer minimumServiceMonths) { this.minimumServiceMonths = minimumServiceMonths; }

    public Double getMaximumIncrementPercentage() { return maximumIncrementPercentage; }
    public void setMaximumIncrementPercentage(Double maximumIncrementPercentage) { this.maximumIncrementPercentage = maximumIncrementPercentage; }

    public Double getMinimumIncrementPercentage() { return minimumIncrementPercentage; }
    public void setMinimumIncrementPercentage(Double minimumIncrementPercentage) { this.minimumIncrementPercentage = minimumIncrementPercentage; }

    public EffectiveDateRule getEffectiveDateRule() { return effectiveDateRule; }
    public void setEffectiveDateRule(EffectiveDateRule effectiveDateRule) { this.effectiveDateRule = effectiveDateRule; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<PolicyBandDto> getBands() { return bands; }
    public void setBands(List<PolicyBandDto> bands) { this.bands = bands; }
}
