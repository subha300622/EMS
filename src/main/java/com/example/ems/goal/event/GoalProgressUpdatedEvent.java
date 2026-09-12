package com.example.ems.goal.event;

public class GoalProgressUpdatedEvent {

    private final Long goalId;
    private final Long organizationId;
    private final Integer previousProgress;
    private final Integer newProgress;
    private final Long updatedBy;

    public GoalProgressUpdatedEvent(Long goalId, Long organizationId, Integer previousProgress, Integer newProgress, Long updatedBy) {
        this.goalId = goalId;
        this.organizationId = organizationId;
        this.previousProgress = previousProgress;
        this.newProgress = newProgress;
        this.updatedBy = updatedBy;
    }

    public Long getGoalId() { return goalId; }
    public Long getOrganizationId() { return organizationId; }
    public Integer getPreviousProgress() { return previousProgress; }
    public Integer getNewProgress() { return newProgress; }
    public Long getUpdatedBy() { return updatedBy; }
}
