package com.example.ems.training.dto;

public class TeamSummaryResponse {

    private int totalEmployees;
    private int totalAssigned;
    private int completed;
    private int inProgress;
    private int overdue;
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
