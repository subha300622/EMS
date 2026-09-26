package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "F&F Settlement Multi-Stage Approvals Progress")
public class FnfApprovalsResponse {

    @Schema(description = "F&F settlement record ID", example = "8001")
    private Long fnfId;

    @Schema(description = "Exit record ID", example = "5001")
    private Long exitId;

    @Schema(description = "Overall approval workflow status", example = "IN_PROGRESS")
    private String workflowStatus;

    @Schema(description = "Current active approval sequence step", example = "2")
    private Integer currentStep;

    @Schema(description = "Ordered approval stages details")
    private List<FnfApprovalStageDto> stages;

    public FnfApprovalsResponse() {}

    public FnfApprovalsResponse(Long fnfId, Long exitId, String workflowStatus, Integer currentStep, List<FnfApprovalStageDto> stages) {
        this.fnfId = fnfId;
        this.exitId = exitId;
        this.workflowStatus = workflowStatus;
        this.currentStep = currentStep;
        this.stages = stages;
    }

    public Long getFnfId() { return fnfId; }
    public void setFnfId(Long fnfId) { this.fnfId = fnfId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public String getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(String workflowStatus) { this.workflowStatus = workflowStatus; }

    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }

    public List<FnfApprovalStageDto> getStages() { return stages; }
    public void setStages(List<FnfApprovalStageDto> stages) { this.stages = stages; }
}
