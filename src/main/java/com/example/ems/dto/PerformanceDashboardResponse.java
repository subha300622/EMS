package com.example.ems.dto;

public class PerformanceDashboardResponse {
    // Cycles
    private long totalCycles;
    private long totalActiveCycles;
    private long totalClosedCycles;

    // Goals
    private long totalGoals;
    private long achievedGoals;
    private long inProgressGoals;
    private long missedGoals;
    private double goalCompletionRate;
    private double averageGoalProgress;

    // Reviews
    private long totalReviews;
    private long selfReviews;
    private long managerReviews;
    private long pendingReviews;
    private long finalizedReviews;
    private double averageRating;

    // PIPs
    private long totalPips;
    private long activePips;
    private long completedPips;

    public long getTotalActiveCycles() { return totalActiveCycles; }
    public void setTotalActiveCycles(long totalActiveCycles) { this.totalActiveCycles = totalActiveCycles; }

    public long getTotalGoals() { return totalGoals; }
    public void setTotalGoals(long totalGoals) { this.totalGoals = totalGoals; }

    public long getAchievedGoals() { return achievedGoals; }
    public void setAchievedGoals(long achievedGoals) { this.achievedGoals = achievedGoals; }

    public long getInProgressGoals() { return inProgressGoals; }
    public void setInProgressGoals(long inProgressGoals) { this.inProgressGoals = inProgressGoals; }

    public long getMissedGoals() { return missedGoals; }
    public void setMissedGoals(long missedGoals) { this.missedGoals = missedGoals; }

    public double getGoalCompletionRate() { return goalCompletionRate; }
    public void setGoalCompletionRate(double goalCompletionRate) { this.goalCompletionRate = goalCompletionRate; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public long getFinalizedReviews() { return finalizedReviews; }
    public void setFinalizedReviews(long finalizedReviews) { this.finalizedReviews = finalizedReviews; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public long getActivePips() { return activePips; }
    public void setActivePips(long activePips) { this.activePips = activePips; }

    public long getTotalCycles() { return totalCycles; }
    public void setTotalCycles(long totalCycles) { this.totalCycles = totalCycles; }

    public long getTotalClosedCycles() { return totalClosedCycles; }
    public void setTotalClosedCycles(long totalClosedCycles) { this.totalClosedCycles = totalClosedCycles; }

    public double getAverageGoalProgress() { return averageGoalProgress; }
    public void setAverageGoalProgress(double averageGoalProgress) { this.averageGoalProgress = averageGoalProgress; }

    public long getSelfReviews() { return selfReviews; }
    public void setSelfReviews(long selfReviews) { this.selfReviews = selfReviews; }

    public long getManagerReviews() { return managerReviews; }
    public void setManagerReviews(long managerReviews) { this.managerReviews = managerReviews; }

    public long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(long pendingReviews) { this.pendingReviews = pendingReviews; }

    public long getTotalPips() { return totalPips; }
    public void setTotalPips(long totalPips) { this.totalPips = totalPips; }

    public long getCompletedPips() { return completedPips; }
    public void setCompletedPips(long completedPips) { this.completedPips = completedPips; }
}
