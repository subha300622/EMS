package com.example.ems.approval.dto;

import com.example.ems.approval.entity.ApproverType;
import com.example.ems.approval.entity.WorkflowType;

import java.util.ArrayList;
import java.util.List;

/**
 * Formalized execution plan produced by the Approval Policy Engine.
 */
public class ApprovalPlan {

    private String policyId;
    private String policyName;
    private WorkflowType workflowType;
    private String executionMode; // SEQUENTIAL, PARALLEL, ANY_ONE
    private List<PlanStep> steps = new ArrayList<>();

    public ApprovalPlan() {}

    public ApprovalPlan(String policyId, String policyName, WorkflowType workflowType, String executionMode) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.workflowType = workflowType;
        this.executionMode = executionMode;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public List<PlanStep> getSteps() {
        return steps;
    }

    public void setSteps(List<PlanStep> steps) {
        this.steps = steps;
    }

    public static class PlanStep {
        private Integer level;
        private String stepName;
        private ApproverType approverType;
        private String approverConfig;

        public PlanStep() {}

        public PlanStep(Integer level, String stepName, ApproverType approverType, String approverConfig) {
            this.level = level;
            this.stepName = stepName;
            this.approverType = approverType;
            this.approverConfig = approverConfig;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getStepName() {
            return stepName;
        }

        public void setStepName(String stepName) {
            this.stepName = stepName;
        }

        public ApproverType getApproverType() {
            return approverType;
        }

        public void setApproverType(ApproverType approverType) {
            this.approverType = approverType;
        }

        public String getApproverConfig() {
            return approverConfig;
        }

        public void setApproverConfig(String approverConfig) {
            this.approverConfig = approverConfig;
        }
    }
}
