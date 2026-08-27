package com.example.ems.approval.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_workflow_steps")
public class ApprovalWorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private ApprovalWorkflowDefinition workflowDefinition;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private ApprovalStepType stepType = ApprovalStepType.USER_APPROVAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_type", nullable = false)
    private ApproverType approverType;

    @Column(name = "approver_config")
    private String approverConfig;

    @Column(nullable = false)
    private Boolean required = true;

    @Column(name = "sla_hours")
    private Integer slaHours = 48;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ApprovalWorkflowStep() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApprovalWorkflowDefinition getWorkflowDefinition() { return workflowDefinition; }
    public void setWorkflowDefinition(ApprovalWorkflowDefinition workflowDefinition) { this.workflowDefinition = workflowDefinition; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public ApprovalStepType getStepType() { return stepType; }
    public void setStepType(ApprovalStepType stepType) { this.stepType = stepType; }

    public ApproverType getApproverType() { return approverType; }
    public void setApproverType(ApproverType approverType) { this.approverType = approverType; }

    public String getApproverConfig() { return approverConfig; }
    public void setApproverConfig(String approverConfig) { this.approverConfig = approverConfig; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
