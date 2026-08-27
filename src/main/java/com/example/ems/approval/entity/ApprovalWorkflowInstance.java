package com.example.ems.approval.entity;

import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_workflow_instances")
public class ApprovalWorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_instance_id", nullable = false, unique = true)
    private String workflowInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id")
    private ApprovalWorkflowDefinition workflowDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false)
    private WorkflowType workflowType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "business_reference_type", nullable = false)
    private String businessReferenceType;

    @Column(name = "business_reference_id", nullable = false)
    private String businessReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 1;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public ApprovalWorkflowInstance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public ApprovalWorkflowDefinition getWorkflowDefinition() { return workflowDefinition; }
    public void setWorkflowDefinition(ApprovalWorkflowDefinition workflowDefinition) { this.workflowDefinition = workflowDefinition; }

    public WorkflowType getWorkflowType() { return workflowType; }
    public void setWorkflowType(WorkflowType workflowType) { this.workflowType = workflowType; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public String getBusinessReferenceType() { return businessReferenceType; }
    public void setBusinessReferenceType(String businessReferenceType) { this.businessReferenceType = businessReferenceType; }

    public String getBusinessReferenceId() { return businessReferenceId; }
    public void setBusinessReferenceId(String businessReferenceId) { this.businessReferenceId = businessReferenceId; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
