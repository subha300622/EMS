package com.example.ems.approval.dto;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;

import java.util.List;

public class ApprovalTaskDetailDto {

    private String approvalTaskId;
    private String workflowInstanceId;
    private WorkflowType workflowType;
    private String businessReferenceType;
    private String businessReferenceId;
    private Integer stepOrder;
    private String stepName;
    private ApprovalStatus status;
    private List<String> availableActions;

    public ApprovalTaskDetailDto() {}

    public ApprovalTaskDetailDto(String approvalTaskId, String workflowInstanceId, WorkflowType workflowType, String businessReferenceType, String businessReferenceId, Integer stepOrder, String stepName, ApprovalStatus status, List<String> availableActions) {
        this.approvalTaskId = approvalTaskId;
        this.workflowInstanceId = workflowInstanceId;
        this.workflowType = workflowType;
        this.businessReferenceType = businessReferenceType;
        this.businessReferenceId = businessReferenceId;
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.status = status;
        this.availableActions = availableActions;
    }

    public String getApprovalTaskId() { return approvalTaskId; }
    public void setApprovalTaskId(String approvalTaskId) { this.approvalTaskId = approvalTaskId; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public WorkflowType getWorkflowType() { return workflowType; }
    public void setWorkflowType(WorkflowType workflowType) { this.workflowType = workflowType; }

    public String getBusinessReferenceType() { return businessReferenceType; }
    public void setBusinessReferenceType(String businessReferenceType) { this.businessReferenceType = businessReferenceType; }

    public String getBusinessReferenceId() { return businessReferenceId; }
    public void setBusinessReferenceId(String businessReferenceId) { this.businessReferenceId = businessReferenceId; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public List<String> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<String> availableActions) { this.availableActions = availableActions; }
}
