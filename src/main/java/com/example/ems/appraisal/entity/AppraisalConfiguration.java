package com.example.ems.appraisal.entity;

import com.example.ems.approval.entity.ApprovalWorkflowDefinition;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_configurations")
public class AppraisalConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiation_mode", nullable = false)
    private AppraisalInitiationMode initiationMode = AppraisalInitiationMode.HR_AND_EMPLOYEE;

    @Column(name = "employee_request_enabled", nullable = false)
    private boolean employeeRequestEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_workflow_id")
    private ApprovalWorkflowDefinition approvalWorkflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_workflow_id")
    private ApprovalWorkflowDefinition reviewWorkflow;

    @Column(name = "min_service_months")
    private Integer minServiceMonths = 6;

    @Column(name = "min_gap_months")
    private Integer minGapMonths = 6;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public AppraisalInitiationMode getInitiationMode() {
        return initiationMode;
    }

    public void setInitiationMode(AppraisalInitiationMode initiationMode) {
        this.initiationMode = initiationMode;
    }

    public boolean isEmployeeRequestEnabled() {
        return employeeRequestEnabled;
    }

    public void setEmployeeRequestEnabled(boolean employeeRequestEnabled) {
        this.employeeRequestEnabled = employeeRequestEnabled;
    }

    public ApprovalWorkflowDefinition getApprovalWorkflow() {
        return approvalWorkflow;
    }

    public void setApprovalWorkflow(ApprovalWorkflowDefinition approvalWorkflow) {
        this.approvalWorkflow = approvalWorkflow;
    }

    public ApprovalWorkflowDefinition getReviewWorkflow() {
        return reviewWorkflow;
    }

    public void setReviewWorkflow(ApprovalWorkflowDefinition reviewWorkflow) {
        this.reviewWorkflow = reviewWorkflow;
    }

    public Integer getMinServiceMonths() {
        return minServiceMonths;
    }

    public void setMinServiceMonths(Integer minServiceMonths) {
        this.minServiceMonths = minServiceMonths;
    }

    public Integer getMinGapMonths() {
        return minGapMonths;
    }

    public void setMinGapMonths(Integer minGapMonths) {
        this.minGapMonths = minGapMonths;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
