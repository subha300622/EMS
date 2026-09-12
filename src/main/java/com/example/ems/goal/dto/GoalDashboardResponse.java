package com.example.ems.goal.dto;

public class GoalDashboardResponse {

    private long totalGoals;
    private long activeGoals;
    private long completedGoals;
    private long overdueGoals;
    private long onTrackGoals;
    private long atRiskGoals;
    private double averageProgress;
    private double completionRate;
    private double totalEstimatedHours;
    private double totalActualHours;
    private double hoursVariance;

    public GoalDashboardResponse() {}

    public long getTotalGoals() { return totalGoals; }
    public void setTotalGoals(long totalGoals) { this.totalGoals = totalGoals; }

    public long getActiveGoals() { return activeGoals; }
    public void setActiveGoals(long activeGoals) { this.activeGoals = activeGoals; }

    public long getCompletedGoals() { return completedGoals; }
    public void setCompletedGoals(long completedGoals) { this.completedGoals = completedGoals; }

    public long getOverdueGoals() { return overdueGoals; }
    public void setOverdueGoals(long overdueGoals) { this.overdueGoals = overdueGoals; }

    public long getOnTrackGoals() { return onTrackGoals; }
    public void setOnTrackGoals(long onTrackGoals) { this.onTrackGoals = onTrackGoals; }

    public long getAtRiskGoals() { return atRiskGoals; }
    public void setAtRiskGoals(long atRiskGoals) { this.atRiskGoals = atRiskGoals; }

    public double getAverageProgress() { return averageProgress; }
    public void setAverageProgress(double averageProgress) { this.averageProgress = averageProgress; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public double getTotalEstimatedHours() { return totalEstimatedHours; }
    public void setTotalEstimatedHours(double totalEstimatedHours) { this.totalEstimatedHours = totalEstimatedHours; }

    public double getTotalActualHours() { return totalActualHours; }
    public void setTotalActualHours(double totalActualHours) { this.totalActualHours = totalActualHours; }

    public double getHoursVariance() { return hoursVariance; }
    public void setHoursVariance(double hoursVariance) { this.hoursVariance = hoursVariance; }
}
