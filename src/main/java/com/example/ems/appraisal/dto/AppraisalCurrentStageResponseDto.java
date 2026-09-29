package com.example.ems.appraisal.dto;

import java.util.List;

public class AppraisalCurrentStageResponseDto {
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private Long cycleId;
    private String cycleName;
    private String appraisalStatus;
    private Integer currentStageOrder;
    private String currentStageName;
    private String requiredPermission;
    private boolean isRequired;
    private Double weightage;
    private boolean canReview;
    private boolean isCompleted;
    private List<ReviewStageDto> completedReviews;

    public AppraisalCurrentStageResponseDto() {}

    public AppraisalCurrentStageResponseDto(Long appraisalId, Long employeeId, String employeeName, Long cycleId,
                                          String cycleName, String appraisalStatus, Integer currentStageOrder,
                                          String currentStageName, String requiredPermission, boolean isRequired,
                                          Double weightage, boolean canReview, boolean isCompleted,
                                          List<ReviewStageDto> completedReviews) {
        this.appraisalId = appraisalId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.cycleId = cycleId;
        this.cycleName = cycleName;
        this.appraisalStatus = appraisalStatus;
        this.currentStageOrder = currentStageOrder;
        this.currentStageName = currentStageName;
        this.requiredPermission = requiredPermission;
        this.isRequired = isRequired;
        this.weightage = weightage;
        this.canReview = canReview;
        this.isCompleted = isCompleted;
        this.completedReviews = completedReviews;
    }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public String getAppraisalStatus() { return appraisalStatus; }
    public void setAppraisalStatus(String appraisalStatus) { this.appraisalStatus = appraisalStatus; }

    public Integer getCurrentStageOrder() { return currentStageOrder; }
    public void setCurrentStageOrder(Integer currentStageOrder) { this.currentStageOrder = currentStageOrder; }

    public String getCurrentStageName() { return currentStageName; }
    public void setCurrentStageName(String currentStageName) { this.currentStageName = currentStageName; }

    public String getRequiredPermission() { return requiredPermission; }
    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }

    public boolean isRequired() { return isRequired; }
    public void setRequired(boolean required) { isRequired = required; }

    public Double getWeightage() { return weightage; }
    public void setWeightage(Double weightage) { this.weightage = weightage; }

    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean canReview) { this.canReview = canReview; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public List<ReviewStageDto> getCompletedReviews() { return completedReviews; }
    public void setCompletedReviews(List<ReviewStageDto> completedReviews) { this.completedReviews = completedReviews; }
}
