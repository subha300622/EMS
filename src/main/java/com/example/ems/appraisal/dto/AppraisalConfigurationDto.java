package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import java.util.List;

public class AppraisalConfigurationDto {
    private Long id;
    private AppraisalInitiationMode initiationMode;
    private boolean employeeRequestEnabled;
    private Long approvalWorkflowId;
    private Long reviewWorkflowId;
    private Integer minServiceMonths;
    private Integer minGapMonths;
    private List<AppraisalRequestReasonDto> allowedRequestReasons;
    private List<ReviewStageConfigurationDto> reviewStages;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppraisalInitiationMode getInitiationMode() { return initiationMode; }
    public void setInitiationMode(AppraisalInitiationMode initiationMode) { this.initiationMode = initiationMode; }

    public boolean isEmployeeRequestEnabled() { return employeeRequestEnabled; }
    public void setEmployeeRequestEnabled(boolean employeeRequestEnabled) { this.employeeRequestEnabled = employeeRequestEnabled; }

    public Long getApprovalWorkflowId() { return approvalWorkflowId; }
    public void setApprovalWorkflowId(Long approvalWorkflowId) { this.approvalWorkflowId = approvalWorkflowId; }

    public Long getReviewWorkflowId() { return reviewWorkflowId; }
    public void setReviewWorkflowId(Long reviewWorkflowId) { this.reviewWorkflowId = reviewWorkflowId; }

    public Integer getMinServiceMonths() { return minServiceMonths; }
    public void setMinServiceMonths(Integer minServiceMonths) { this.minServiceMonths = minServiceMonths; }

    public Integer getMinGapMonths() { return minGapMonths; }
    public void setMinGapMonths(Integer minGapMonths) { this.minGapMonths = minGapMonths; }

    public List<AppraisalRequestReasonDto> getAllowedRequestReasons() { return allowedRequestReasons; }
    public void setAllowedRequestReasons(List<AppraisalRequestReasonDto> allowedRequestReasons) { this.allowedRequestReasons = allowedRequestReasons; }

    public List<ReviewStageConfigurationDto> getReviewStages() { return reviewStages; }
    public void setReviewStages(List<ReviewStageConfigurationDto> reviewStages) { this.reviewStages = reviewStages; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
