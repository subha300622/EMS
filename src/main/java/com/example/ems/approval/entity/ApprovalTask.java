package com.example.ems.approval.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_tasks")
public class ApprovalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_task_id", nullable = false, unique = true)
    private String approvalTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private ApprovalWorkflowInstance workflowInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private ApprovalWorkflowStep step;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_type", nullable = false)
    private WorkflowType workflowType;

    @Column(name = "business_reference_type", nullable = false)
    private String businessReferenceType;

    @Column(name = "business_reference_id", nullable = false)
    private String businessReferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private Employee approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public ApprovalTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApprovalTaskId() { return approvalTaskId; }
    public void setApprovalTaskId(String approvalTaskId) { this.approvalTaskId = approvalTaskId; }

    public ApprovalWorkflowInstance getWorkflowInstance() { return workflowInstance; }
    public void setWorkflowInstance(ApprovalWorkflowInstance workflowInstance) { this.workflowInstance = workflowInstance; }

    public ApprovalWorkflowStep getStep() { return step; }
    public void setStep(ApprovalWorkflowStep step) { this.step = step; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public WorkflowType getWorkflowType() { return workflowType; }
    public void setWorkflowType(WorkflowType workflowType) { this.workflowType = workflowType; }

    public String getBusinessReferenceType() { return businessReferenceType; }
    public void setBusinessReferenceType(String businessReferenceType) { this.businessReferenceType = businessReferenceType; }

    public String getBusinessReferenceId() { return businessReferenceId; }
    public void setBusinessReferenceId(String businessReferenceId) { this.businessReferenceId = businessReferenceId; }

    public Employee getApprover() { return approver; }
    public void setApprover(Employee approver) { this.approver = approver; }

    public ApprovalStatus getStatus() { return status; }
    public void setStatus(ApprovalStatus status) { this.status = status; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
