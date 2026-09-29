package com.example.ems.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_approval_policies")
public class OnboardingApprovalPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "policy_id", nullable = false, length = 50)
    private String policyId;

    @Column(name = "current_status", nullable = false, length = 50)
    private String currentStatus;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "next_status", nullable = false, length = 50)
    private String nextStatus;

    @Column(name = "approver_type", nullable = false, length = 50)
    private String approverType; // EMPLOYEE, MANAGER, REPORTING_MANAGER, DEPARTMENT_HEAD, HR_OWNER, CONFIGURED_ROLE

    @Column(name = "approver_role_id")
    private Long approverRoleId;

    @Column(name = "organization_configurable", nullable = false)
    private boolean organizationConfigurable = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "conditions")
    private String conditions;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNextStatus() { return nextStatus; }
    public void setNextStatus(String nextStatus) { this.nextStatus = nextStatus; }

    public String getApproverType() { return approverType; }
    public void setApproverType(String approverType) { this.approverType = approverType; }

    public Long getApproverRoleId() { return approverRoleId; }
    public void setApproverRoleId(Long approverRoleId) { this.approverRoleId = approverRoleId; }

    public boolean isOrganizationConfigurable() { return organizationConfigurable; }
    public void setOrganizationConfigurable(boolean organizationConfigurable) { this.organizationConfigurable = organizationConfigurable; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
