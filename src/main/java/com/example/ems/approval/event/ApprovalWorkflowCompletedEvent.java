package com.example.ems.approval.event;

import com.example.ems.approval.entity.WorkflowType;
import org.springframework.context.ApplicationEvent;

public class ApprovalWorkflowCompletedEvent extends ApplicationEvent {

    private final String workflowInstanceId;
    private final WorkflowType workflowType;
    private final String businessReferenceType;
    private final String businessReferenceId;
    private final Long organizationId;

    public ApprovalWorkflowCompletedEvent(
            Object source,
            String workflowInstanceId,
            WorkflowType workflowType,
            String businessReferenceType,
            String businessReferenceId,
            Long organizationId) {
        super(source);
        this.workflowInstanceId = workflowInstanceId;
        this.workflowType = workflowType;
        this.businessReferenceType = businessReferenceType;
        this.businessReferenceId = businessReferenceId;
        this.organizationId = organizationId;
    }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public WorkflowType getWorkflowType() { return workflowType; }
    public String getBusinessReferenceType() { return businessReferenceType; }
    public String getBusinessReferenceId() { return businessReferenceId; }
    public Long getOrganizationId() { return organizationId; }
}
