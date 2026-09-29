package com.example.ems.goal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_goal_config_org", columnNames = {"organization_id"})
})
public class GoalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "min_weightage")
    private Integer minWeightage = 1;

    @Column(name = "max_weightage")
    private Integer maxWeightage = 100;

    @Column(name = "allow_milestone_progress_calc")
    private Boolean allowMilestoneProgressCalc = true;

    @Column(name = "allow_auto_reassignment")
    private Boolean allowAutoReassignment = true;

    @Column(name = "require_approval_for_create")
    private Boolean requireApprovalForCreate = false;

    @Column(name = "require_approval_for_complete")
    private Boolean requireApprovalForComplete = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GoalConfig() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Integer getMinWeightage() { return minWeightage; }
    public void setMinWeightage(Integer minWeightage) { this.minWeightage = minWeightage; }

    public Integer getMaxWeightage() { return maxWeightage; }
    public void setMaxWeightage(Integer maxWeightage) { this.maxWeightage = maxWeightage; }

    public Boolean getAllowMilestoneProgressCalc() { return allowMilestoneProgressCalc; }
    public void setAllowMilestoneProgressCalc(Boolean allowMilestoneProgressCalc) { this.allowMilestoneProgressCalc = allowMilestoneProgressCalc; }

    public Boolean getAllowAutoReassignment() { return allowAutoReassignment; }
    public void setAllowAutoReassignment(Boolean allowAutoReassignment) { this.allowAutoReassignment = allowAutoReassignment; }

    public Boolean getRequireApprovalForCreate() { return requireApprovalForCreate; }
    public void setRequireApprovalForCreate(Boolean requireApprovalForCreate) { this.requireApprovalForCreate = requireApprovalForCreate; }

    public Boolean getRequireApprovalForComplete() { return requireApprovalForComplete; }
    public void setRequireApprovalForComplete(Boolean requireApprovalForComplete) { this.requireApprovalForComplete = requireApprovalForComplete; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
