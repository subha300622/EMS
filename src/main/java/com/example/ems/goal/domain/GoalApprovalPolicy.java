package com.example.ems.goal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_approval_policies")
public class GoalApprovalPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(length = 50)
    private String module = "GOAL";

    @Column(nullable = false, length = 50)
    private String action; // CREATE, COMPLETE

    @Column(name = "approval_required")
    private Boolean approvalRequired = true;

    @Column(name = "approval_type", length = 50)
    private String approvalType = "MANAGER"; // MANAGER, HR, MULTI_LEVEL, ADMIN

    @Column(length = 50)
    private String priority; // HIGH, CRITICAL, ALL

    @Column(name = "goal_type", length = 50)
    private String goalType; // ORGANIZATION, DEPARTMENT, TEAM, INDIVIDUAL, ALL

    @Column(name = "weightage_threshold")
    private Integer weightageThreshold = 0;

    @Column(name = "estimated_hours_threshold")
    private Double estimatedHoursThreshold = 0.0;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "approver_role", length = 50)
    private String approverRole; // REPORTING_MANAGER, DEPARTMENT_HEAD, HR, ADMIN

    @Column(name = "approval_levels")
    private Integer approvalLevels = 1;

    @Column(name = "auto_approval")
    private Boolean autoApproval = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GoalApprovalPolicy() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public String getApprovalType() { return approvalType; }
    public void setApprovalType(String approvalType) { this.approvalType = approvalType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public Integer getWeightageThreshold() { return weightageThreshold; }
    public void setWeightageThreshold(Integer weightageThreshold) { this.weightageThreshold = weightageThreshold; }

    public Double getEstimatedHoursThreshold() { return estimatedHoursThreshold; }
    public void setEstimatedHoursThreshold(Double estimatedHoursThreshold) { this.estimatedHoursThreshold = estimatedHoursThreshold; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public Integer getApprovalLevels() { return approvalLevels; }
    public void setApprovalLevels(Integer approvalLevels) { this.approvalLevels = approvalLevels; }

    public Boolean getAutoApproval() { return autoApproval; }
    public void setAutoApproval(Boolean autoApproval) { this.autoApproval = autoApproval; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
