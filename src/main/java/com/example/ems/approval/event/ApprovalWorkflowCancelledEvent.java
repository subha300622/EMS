package com.example.ems.approval.event;

import com.example.ems.approval.entity.WorkflowType;
import org.springframework.context.ApplicationEvent;

public class ApprovalWorkflowCancelledEvent extends ApplicationEvent {

    private final String workflowInstanceId;
    private final WorkflowType workflowType;
    private final String businessReferenceType;
    private final String businessReferenceId;
    private final Long organizationId;
    private final String reason;

    public ApprovalWorkflowCancelledEvent(
            Object source,
            String workflowInstanceId,
            WorkflowType workflowType,
            String businessReferenceType,
            String businessReferenceId,
            Long organizationId,
            String reason) {
        super(source);
        this.workflowInstanceId = workflowInstanceId;
        this.workflowType = workflowType;
        this.businessReferenceType = businessReferenceType;
        this.businessReferenceId = businessReferenceId;
        this.organizationId = organizationId;
        this.reason = reason;
    }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public WorkflowType getWorkflowType() { return workflowType; }
    public String getBusinessReferenceType() { return businessReferenceType; }
    public String getBusinessReferenceId() { return businessReferenceId; }
    public Long getOrganizationId() { return organizationId; }
    public String getReason() { return reason; }
}
