package com.example.ems.training.dto;

public class TeamProgressResponse {
    private String teamId;
    private String teamName;
    private long totalEmployees;
    private long assignedEmployees;
    private long completed;
    private long inProgress;
    private long pending;
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
