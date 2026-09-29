package com.example.ems.appraisal.dto;

import java.time.LocalDateTime;

public class AppraisalEmployeeDashboardDto {
    private Long employeeId;
    private String employeeName;
    private Long currentAppraisalId;
    private String currentAppraisalStatus;
    private Integer currentStageOrder;
    private String currentStageName;
    private Double selfRating;
    private Double finalRating;
    private String performanceCategory;
    private LocalDateTime publishedAt;
    private Double approvedIncrementPercentage;
    private Double approvedBonusPercentage;
    private String incrementStatus;
    private int totalHistoricalAppraisals;

    public AppraisalEmployeeDashboardDto() {}

    public AppraisalEmployeeDashboardDto(Long employeeId, String employeeName, Long currentAppraisalId,
                                        String currentAppraisalStatus, Integer currentStageOrder,
                                        String currentStageName, Double selfRating, Double finalRating,
                                        String performanceCategory, LocalDateTime publishedAt,
                                        Double approvedIncrementPercentage, Double approvedBonusPercentage,
                                        String incrementStatus, int totalHistoricalAppraisals) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.currentAppraisalId = currentAppraisalId;
        this.currentAppraisalStatus = currentAppraisalStatus;
        this.currentStageOrder = currentStageOrder;
        this.currentStageName = currentStageName;
        this.selfRating = selfRating;
        this.finalRating = finalRating;
        this.performanceCategory = performanceCategory;
        this.publishedAt = publishedAt;
        this.approvedIncrementPercentage = approvedIncrementPercentage;
        this.approvedBonusPercentage = approvedBonusPercentage;
        this.incrementStatus = incrementStatus;
        this.totalHistoricalAppraisals = totalHistoricalAppraisals;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getCurrentAppraisalId() { return currentAppraisalId; }
    public void setCurrentAppraisalId(Long currentAppraisalId) { this.currentAppraisalId = currentAppraisalId; }

    public String getCurrentAppraisalStatus() { return currentAppraisalStatus; }
    public void setCurrentAppraisalStatus(String currentAppraisalStatus) { this.currentAppraisalStatus = currentAppraisalStatus; }

    public Integer getCurrentStageOrder() { return currentStageOrder; }
    public void setCurrentStageOrder(Integer currentStageOrder) { this.currentStageOrder = currentStageOrder; }

    public String getCurrentStageName() { return currentStageName; }
    public void setCurrentStageName(String currentStageName) { this.currentStageName = currentStageName; }

    public Double getSelfRating() { return selfRating; }
    public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public String getPerformanceCategory() { return performanceCategory; }
    public void setPerformanceCategory(String performanceCategory) { this.performanceCategory = performanceCategory; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Double getApprovedIncrementPercentage() { return approvedIncrementPercentage; }
    public void setApprovedIncrementPercentage(Double approvedIncrementPercentage) { this.approvedIncrementPercentage = approvedIncrementPercentage; }

    public Double getApprovedBonusPercentage() { return approvedBonusPercentage; }
    public void setApprovedBonusPercentage(Double approvedBonusPercentage) { this.approvedBonusPercentage = approvedBonusPercentage; }

    public String getIncrementStatus() { return incrementStatus; }
    public void setIncrementStatus(String incrementStatus) { this.incrementStatus = incrementStatus; }

    public int getTotalHistoricalAppraisals() { return totalHistoricalAppraisals; }
    public void setTotalHistoricalAppraisals(int totalHistoricalAppraisals) { this.totalHistoricalAppraisals = totalHistoricalAppraisals; }
}
