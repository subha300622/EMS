package com.example.ems.increment.dto;

import com.example.ems.increment.entity.EffectiveDateRule;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateIncrementPolicyRequest {

    @NotBlank(message = "Policy name is required")
    private String name;

    private Boolean appraisalRequired = false;

    @DecimalMin(value = "0.0", message = "Minimum rating must be >= 0")
    private Double minimumRating = 3.0;

    @DecimalMin(value = "0.0", message = "Minimum goal achievement must be >= 0")
    @DecimalMax(value = "100.0", message = "Minimum goal achievement must be <= 100")
    private Double minimumGoalAchievementPercentage = 70.0;

    @DecimalMin(value = "0.0", message = "Minimum attendance must be >= 0")
    @DecimalMax(value = "100.0", message = "Minimum attendance must be <= 100")
    private Double minimumAttendancePercentage = 90.0;

    private Integer minimumServiceMonths = 12;

    @DecimalMin(value = "0.01", message = "Maximum increment percentage must be > 0")
    private Double maximumIncrementPercentage = 20.0;

    @DecimalMin(value = "0.0", message = "Minimum increment percentage must be >= 0")
    private Double minimumIncrementPercentage = 0.0;

    private EffectiveDateRule effectiveDateRule = EffectiveDateRule.FIXED_DATE;

    private LocalDate effectiveDate;

    @DecimalMin(value = "0.0", message = "Budget limit must be >= 0")
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    private Boolean active = true;

    private List<PolicyBandDto> bands;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getAppraisalRequired() { return appraisalRequired != null ? appraisalRequired : false; }
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
