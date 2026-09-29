package com.example.ems.appraisal.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppraisalConfigurationSnapshotDto {

    private Integer versionNumber;
    private String initiationMode;
    private Boolean employeeRequestEnabled;
    private Integer minServiceMonths;
    private Integer minGapMonths;
    private List<ReviewStageConfigurationDto> reviewStages = new ArrayList<>();
    private RatingScaleDto ratingScale;
    private List<AppraisalCriterionDto> criteria = new ArrayList<>();
    private List<PerformanceCategoryDto> performanceCategories = new ArrayList<>();
    private IncrementPolicyDto incrementPolicy;
    private LocalDateTime snapshottedAt = LocalDateTime.now();

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getInitiationMode() { return initiationMode; }
    public void setInitiationMode(String initiationMode) { this.initiationMode = initiationMode; }

    public Boolean getEmployeeRequestEnabled() { return employeeRequestEnabled; }
    public void setEmployeeRequestEnabled(Boolean employeeRequestEnabled) { this.employeeRequestEnabled = employeeRequestEnabled; }

    public Integer getMinServiceMonths() { return minServiceMonths; }
    public void setMinServiceMonths(Integer minServiceMonths) { this.minServiceMonths = minServiceMonths; }

    public Integer getMinGapMonths() { return minGapMonths; }
    public void setMinGapMonths(Integer minGapMonths) { this.minGapMonths = minGapMonths; }

    public List<ReviewStageConfigurationDto> getReviewStages() { return reviewStages; }
    public void setReviewStages(List<ReviewStageConfigurationDto> reviewStages) { this.reviewStages = reviewStages; }

    public RatingScaleDto getRatingScale() { return ratingScale; }
    public void setRatingScale(RatingScaleDto ratingScale) { this.ratingScale = ratingScale; }

    public List<AppraisalCriterionDto> getCriteria() { return criteria; }
    public void setCriteria(List<AppraisalCriterionDto> criteria) { this.criteria = criteria; }

    public List<PerformanceCategoryDto> getPerformanceCategories() { return performanceCategories; }
    public void setPerformanceCategories(List<PerformanceCategoryDto> performanceCategories) { this.performanceCategories = performanceCategories; }

    public IncrementPolicyDto getIncrementPolicy() { return incrementPolicy; }
    public void setIncrementPolicy(IncrementPolicyDto incrementPolicy) { this.incrementPolicy = incrementPolicy; }

    public LocalDateTime getSnapshottedAt() { return snapshottedAt; }
    public void setSnapshottedAt(LocalDateTime snapshottedAt) { this.snapshottedAt = snapshottedAt; }
}
