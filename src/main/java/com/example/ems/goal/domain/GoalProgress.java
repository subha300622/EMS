package com.example.ems.goal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_progress")
public class GoalProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(nullable = false)
    private Integer percentage;

    @Column(name = "target_achieved")
    private Double targetAchieved;

    @Column(name = "milestone_completed")
    private Integer milestoneCompleted;

    @Column(name = "tasks_completed")
    private Integer tasksCompleted;

    @Column(name = "update_comment", columnDefinition = "TEXT")
    private String updateComment;

    @Column(name = "evidence_document")
    private String evidenceDocument;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GoalProgress() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }

    public Double getTargetAchieved() { return targetAchieved; }
    public void setTargetAchieved(Double targetAchieved) { this.targetAchieved = targetAchieved; }

    public Integer getMilestoneCompleted() { return milestoneCompleted; }
    public void setMilestoneCompleted(Integer milestoneCompleted) { this.milestoneCompleted = milestoneCompleted; }

    public Integer getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(Integer tasksCompleted) { this.tasksCompleted = tasksCompleted; }

    public String getUpdateComment() { return updateComment; }
    public void setUpdateComment(String updateComment) { this.updateComment = updateComment; }

    public String getEvidenceDocument() { return evidenceDocument; }
    public void setEvidenceDocument(String evidenceDocument) { this.evidenceDocument = evidenceDocument; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
