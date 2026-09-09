package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Team Training Progress Response")
public class TeamProgressResponse {
    @Schema(description = "Team ID", example = "1")
    private String teamId;
    @Schema(description = "Team name", example = "Backend Core")
    private String teamName;
    @Schema(description = "Total number of employees in team", example = "10")
    private long totalEmployees;
    @Schema(description = "Number of employees assigned to trainings", example = "10")
    private long assignedEmployees;
    @Schema(description = "Number of completed trainings", example = "8")
    private long completed;
    @Schema(description = "Number of trainings in progress", example = "2")
    private long inProgress;
    @Schema(description = "Number of pending trainings", example = "0")
    private long pending;
    @Schema(description = "Percentage of trainings completed", example = "80.00")
    private double completionPercentage;

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getAssignedEmployees() { return assignedEmployees; }
    public void setAssignedEmployees(long assignedEmployees) { this.assignedEmployees = assignedEmployees; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
