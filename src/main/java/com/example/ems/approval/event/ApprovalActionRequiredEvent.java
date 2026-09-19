package com.example.ems.approval.event;

import com.example.ems.approval.entity.WorkflowType;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an approval task is created and requires action by an approver.
 * Transport-independent contract ready for in-process Spring events today and Outbox/Kafka in the future.
 */
public class ApprovalActionRequiredEvent extends ApplicationEvent {

    private final String approvalTaskId;
    private final String workflowInstanceId;
    private final WorkflowType workflowType;
    private final String businessReferenceType;
    private final String businessReferenceId;
    private final Long organizationId;
    private final Integer stageOrder;
    private final String stageName;
    private final Long approverEmployeeId;

    public ApprovalActionRequiredEvent(
            Object source,
            String approvalTaskId,
            String workflowInstanceId,
            WorkflowType workflowType,
            String businessReferenceType,
            String businessReferenceId,
            Long organizationId,
            Integer stageOrder,
            String stageName,
            Long approverEmployeeId) {
        super(source);
        this.approvalTaskId = approvalTaskId;
        this.workflowInstanceId = workflowInstanceId;
        this.workflowType = workflowType;
        this.businessReferenceType = businessReferenceType;
        this.businessReferenceId = businessReferenceId;
        this.organizationId = organizationId;
        this.stageOrder = stageOrder;
        this.stageName = stageName;
        this.approverEmployeeId = approverEmployeeId;
    }

    public String getApprovalTaskId() {
        return approvalTaskId;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public String getBusinessReferenceType() {
        return businessReferenceType;
    }

    public String getBusinessReferenceId() {
        return businessReferenceId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public String getStageName() {
        return stageName;
    }

    public Long getApproverEmployeeId() {
        return approverEmployeeId;
    }
}
