package com.example.ems.increment.dto;

import com.example.ems.increment.entity.EffectiveDateRule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Increment Policy Response")
public class IncrementPolicyResponse {

    @Schema(description = "Policy ID", example = "1")
    private Long id;
    @Schema(description = "Policy Name", example = "Standard Annual Increment Policy")
    private String name;
    @Schema(description = "Policy Version", example = "1")
    private Integer version;
    @Schema(description = "Is appraisal mandatory", example = "true")
    private Boolean appraisalRequired;
    @Schema(description = "Minimum appraisal rating threshold", example = "3.5")
    private Double minimumRating;
    @Schema(description = "Minimum goal achievement percentage", example = "75.0")
    private Double minimumGoalAchievementPercentage;
    @Schema(description = "Minimum attendance percentage", example = "85.0")
    private Double minimumAttendancePercentage;
    @Schema(description = "Minimum service months required", example = "12")
    private Integer minimumServiceMonths;
    @Schema(description = "Maximum increment percentage cap", example = "25.0")
    private Double maximumIncrementPercentage;
    @Schema(description = "Minimum increment percentage floor", example = "3.0")
    private Double minimumIncrementPercentage;
    @Schema(description = "Effective date rule type", example = "FIXED_DATE")
    private EffectiveDateRule effectiveDateRule;
    @Schema(description = "Effective date", example = "2026-04-01")
    private LocalDate effectiveDate;
    @Schema(description = "Total budget limit", example = "1000000.00")
    private BigDecimal budgetLimit;
    @Schema(description = "Is policy currently active", example = "true")
    private Boolean active;
    @Schema(description = "Policy rating/increment bands")
    private List<PolicyBandDto> bands;
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
