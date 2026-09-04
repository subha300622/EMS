package com.example.ems.goal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_assignment_history")
public class GoalAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "previous_owner_id")
    private Long previousOwnerId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "assignment_level")
    private String assignmentLevel;

    @Column(name = "assignment_type")
    private String assignmentType;

    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    public GoalAssignmentHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }

    public Long getPreviousOwnerId() { return previousOwnerId; }
    public void setPreviousOwnerId(Long previousOwnerId) { this.previousOwnerId = previousOwnerId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getAssignmentLevel() { return assignmentLevel; }
    public void setAssignmentLevel(String assignmentLevel) { this.assignmentLevel = assignmentLevel; }

    public String getAssignmentType() { return assignmentType; }
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

    public String getAssignmentReason() { return assignmentReason; }
    public void setAssignmentReason(String assignmentReason) { this.assignmentReason = assignmentReason; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getUnassignedAt() { return unassignedAt; }
    public void setUnassignedAt(LocalDateTime unassignedAt) { this.unassignedAt = unassignedAt; }
}
