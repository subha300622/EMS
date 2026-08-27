package com.example.ems.approval.dto;

import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.WorkflowType;

public class ApprovalTaskDto {

    private String approvalTaskId;
    private String workflowInstanceId;
    private WorkflowType workflowType;
    private String businessReferenceType;
    private String businessReferenceId;
    private Integer stepOrder;
    private String stepName;
    private String approverId;
    private String approverName;
    private ApprovalStatus status;
    private String assignedAt;
    private String dueAt;

    public ApprovalTaskDto() {}

    public ApprovalTaskDto(String approvalTaskId, String workflowInstanceId, WorkflowType workflowType, String businessReferenceType, String businessReferenceId, Integer stepOrder, String stepName, String approverId, String approverName, ApprovalStatus status, String assignedAt, String dueAt) {
        this.approvalTaskId = approvalTaskId;
        this.workflowInstanceId = workflowInstanceId;
        this.workflowType = workflowType;
        this.businessReferenceType = businessReferenceType;
        this.businessReferenceId = businessReferenceId;
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.approverId = approverId;
        this.approverName = approverName;
        this.status = status;
        this.assignedAt = assignedAt;
        this.dueAt = dueAt;
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

    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public String getDueAt() { return dueAt; }
    public void setDueAt(String dueAt) { this.dueAt = dueAt; }
}
