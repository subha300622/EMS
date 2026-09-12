package com.example.ems.approval.dto;

import com.example.ems.approval.entity.ApproverType;
import com.example.ems.approval.entity.WorkflowType;
import java.util.List;

public class CreateApprovalWorkflowRequest {

    private String name;
    private WorkflowType workflowType; // e.g. LEAVE_APPROVAL
    private String entityType;          // alias for workflowType if sent as String
    private boolean enabled = true;
    private List<StepRequest> steps;

    public static class StepRequest {
        private Integer sequence;
        private Integer stepOrder;
        private ApproverType approverType;
        private String approverConfig;
        private Integer slaHours;

        public Integer getSequence() { return sequence != null ? sequence : stepOrder; }
        public void setSequence(Integer sequence) { this.sequence = sequence; }

        public Integer getStepOrder() { return stepOrder != null ? stepOrder : sequence; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

        public ApproverType getApproverType() { return approverType; }
        public void setApproverType(ApproverType approverType) { this.approverType = approverType; }

        public String getApproverConfig() { return approverConfig; }
        public void setApproverConfig(String approverConfig) { this.approverConfig = approverConfig; }

        public Integer getSlaHours() { return slaHours; }
        public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WorkflowType getWorkflowType() {
        if (workflowType != null) return workflowType;
        if (entityType != null) {
            try {
                return WorkflowType.valueOf(entityType.toUpperCase());
            } catch (Exception ignored) {}
        }
        return WorkflowType.LEAVE_APPROVAL;
    }
    public void setWorkflowType(WorkflowType workflowType) { this.workflowType = workflowType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<StepRequest> getSteps() { return steps; }
    public void setSteps(List<StepRequest> steps) { this.steps = steps; }
}
