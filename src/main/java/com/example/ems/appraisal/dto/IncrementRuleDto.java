package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotNull;

public class IncrementRuleDto {

    private Long id;
    private Long policyId;
    private Long performanceCategoryId;
    private String performanceCategoryName;

    @NotNull(message = "minRating is required")
    private Double minRating;

    @NotNull(message = "maxRating is required")
    private Double maxRating;

    private Boolean eligible = true;

    @NotNull(message = "incrementPercentage is required")
    private Double incrementPercentage = 0.0;

    private Double bonusPercentage = 0.0;
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }

    public Long getPerformanceCategoryId() { return performanceCategoryId; }
    public void setPerformanceCategoryId(Long performanceCategoryId) { this.performanceCategoryId = performanceCategoryId; }

    public String getPerformanceCategoryName() { return performanceCategoryName; }
    public void setPerformanceCategoryName(String performanceCategoryName) { this.performanceCategoryName = performanceCategoryName; }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public Double getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(Double incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public Double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(Double bonusPercentage) { this.bonusPercentage = bonusPercentage; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
