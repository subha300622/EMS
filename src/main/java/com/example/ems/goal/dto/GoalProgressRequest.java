package com.example.ems.goal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GoalProgressRequest {

    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Percentage cannot be negative")
    @Max(value = 100, message = "Percentage cannot exceed 100")
    private Integer percentage;

    private Double targetAchieved;
    private Integer milestoneCompleted;
    private Integer tasksCompleted;
    private String updateComment;
    private String evidenceDocument;

    public GoalProgressRequest() {}

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
}
