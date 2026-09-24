package com.example.ems.appraisal.dto;

import java.util.Map;

public class AppraisalOrganizationDashboardDto {
    private int totalCycles;
    private int openCycles;
    private Long activeCycleId;
    private String activeCycleName;
    private int totalAppraisals;
    private Map<String, Long> statusBreakdown;
    private Double averageRating;
    private Map<String, Long> performanceCategoryDistribution;
    private int totalIncrementsApproved;
    private int totalIncrementsApplied;

    public AppraisalOrganizationDashboardDto() {}

    public AppraisalOrganizationDashboardDto(int totalCycles, int openCycles, Long activeCycleId,
                                           String activeCycleName, int totalAppraisals,
                                           Map<String, Long> statusBreakdown, Double averageRating,
                                           Map<String, Long> performanceCategoryDistribution,
                                           int totalIncrementsApproved, int totalIncrementsApplied) {
        this.totalCycles = totalCycles;
        this.openCycles = openCycles;
        this.activeCycleId = activeCycleId;
        this.activeCycleName = activeCycleName;
        this.totalAppraisals = totalAppraisals;
        this.statusBreakdown = statusBreakdown;
        this.averageRating = averageRating;
        this.performanceCategoryDistribution = performanceCategoryDistribution;
        this.totalIncrementsApproved = totalIncrementsApproved;
        this.totalIncrementsApplied = totalIncrementsApplied;
    }

    public int getTotalCycles() { return totalCycles; }
    public void setTotalCycles(int totalCycles) { this.totalCycles = totalCycles; }

    public int getOpenCycles() { return openCycles; }
    public void setOpenCycles(int openCycles) { this.openCycles = openCycles; }

    public Long getActiveCycleId() { return activeCycleId; }
    public void setActiveCycleId(Long activeCycleId) { this.activeCycleId = activeCycleId; }

    public String getActiveCycleName() { return activeCycleName; }
    public void setActiveCycleName(String activeCycleName) { this.activeCycleName = activeCycleName; }

    public int getTotalAppraisals() { return totalAppraisals; }
    public void setTotalAppraisals(int totalAppraisals) { this.totalAppraisals = totalAppraisals; }

    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Map<String, Long> getPerformanceCategoryDistribution() { return performanceCategoryDistribution; }
    public void setPerformanceCategoryDistribution(Map<String, Long> performanceCategoryDistribution) { this.performanceCategoryDistribution = performanceCategoryDistribution; }

    public int getTotalIncrementsApproved() { return totalIncrementsApproved; }
    public void setTotalIncrementsApproved(int totalIncrementsApproved) { this.totalIncrementsApproved = totalIncrementsApproved; }

    public int getTotalIncrementsApplied() { return totalIncrementsApplied; }
    public void setTotalIncrementsApplied(int totalIncrementsApplied) { this.totalIncrementsApplied = totalIncrementsApplied; }
}
