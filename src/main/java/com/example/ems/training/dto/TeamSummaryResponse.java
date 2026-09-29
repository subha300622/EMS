package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Team Training Summary Response")
public class TeamSummaryResponse {

    @Schema(description = "Total number of employees in the team", example = "10")
    private int totalEmployees;
    @Schema(description = "Total number of trainings assigned", example = "25")
    private int totalAssigned;
    @Schema(description = "Number of completed trainings", example = "18")
    private int completed;
    @Schema(description = "Number of trainings in progress", example = "5")
    private int inProgress;
    @Schema(description = "Number of overdue trainings", example = "2")
    private int overdue;
    @Schema(description = "Compliance rate percentage", example = "72.00")
    private double complianceRate;

    public TeamSummaryResponse() {}

    public TeamSummaryResponse(int totalEmployees, int totalAssigned, int completed, int inProgress, int overdue, double complianceRate) {
        this.totalEmployees = totalEmployees;
        this.totalAssigned = totalAssigned;
        this.completed = completed;
        this.inProgress = inProgress;
        this.overdue = overdue;
        this.complianceRate = complianceRate;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }

    public void setTotalAssigned(int totalAssigned) {
        this.totalAssigned = totalAssigned;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getInProgress() {
        return inProgress;
    }

    public void setInProgress(int inProgress) {
        this.inProgress = inProgress;
    }

    public int getOverdue() {
        return overdue;
    }

    public void setOverdue(int overdue) {
        this.overdue = overdue;
    }

    public double getComplianceRate() {
        return complianceRate;
    }

    public void setComplianceRate(double complianceRate) {
        this.complianceRate = complianceRate;
    }
}
