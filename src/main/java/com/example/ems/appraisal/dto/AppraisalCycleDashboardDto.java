package com.example.ems.appraisal.dto;

import java.util.Map;

public class AppraisalCycleDashboardDto {
    private Long cycleId;
    private String cycleName;
    private String cycleStatus;
    private int totalAppraisals;
    private int completedCount;
    private int publishedCount;
    private double completionPercentage;
    private Double averageRating;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> stageBreakdown;
    private Map<String, Long> categoryDistribution;

    public AppraisalCycleDashboardDto() {}

    public AppraisalCycleDashboardDto(Long cycleId, String cycleName, String cycleStatus, int totalAppraisals,
                                     int completedCount, int publishedCount, double completionPercentage,
                                     Double averageRating, Map<String, Long> statusBreakdown,
                                     Map<String, Long> stageBreakdown, Map<String, Long> categoryDistribution) {
        this.cycleId = cycleId;
        this.cycleName = cycleName;
        this.cycleStatus = cycleStatus;
        this.totalAppraisals = totalAppraisals;
        this.completedCount = completedCount;
        this.publishedCount = publishedCount;
        this.completionPercentage = completionPercentage;
        this.averageRating = averageRating;
        this.statusBreakdown = statusBreakdown;
        this.stageBreakdown = stageBreakdown;
        this.categoryDistribution = categoryDistribution;
    }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public String getCycleStatus() { return cycleStatus; }
    public void setCycleStatus(String cycleStatus) { this.cycleStatus = cycleStatus; }

    public int getTotalAppraisals() { return totalAppraisals; }
    public void setTotalAppraisals(int totalAppraisals) { this.totalAppraisals = totalAppraisals; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getPublishedCount() { return publishedCount; }
    public void setPublishedCount(int publishedCount) { this.publishedCount = publishedCount; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }

    public Map<String, Long> getStageBreakdown() { return stageBreakdown; }
    public void setStageBreakdown(Map<String, Long> stageBreakdown) { this.stageBreakdown = stageBreakdown; }

    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
}
