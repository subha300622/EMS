package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SaveAppraisalConfigurationRequest {

    @NotNull(message = "initiationMode is required")
    private AppraisalInitiationMode initiationMode;

    private boolean employeeRequestEnabled = true;

    private Long approvalWorkflowId;

    private Long reviewWorkflowId;

    private Integer minServiceMonths;

    private Integer minGapMonths;

    private List<CreateRequestReasonDto> allowedRequestReasons;

    private List<ReviewStageConfigurationDto> reviewStages;

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

    public List<CreateRequestReasonDto> getAllowedRequestReasons() { return allowedRequestReasons; }
    public void setAllowedRequestReasons(List<CreateRequestReasonDto> allowedRequestReasons) { this.allowedRequestReasons = allowedRequestReasons; }

    public List<ReviewStageConfigurationDto> getReviewStages() { return reviewStages; }
    public void setReviewStages(List<ReviewStageConfigurationDto> reviewStages) { this.reviewStages = reviewStages; }
}
