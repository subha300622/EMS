package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class PerformanceDashboardResponse {
    // Cycles
    @Schema(example = "1")
    private long totalCycles;
    @Schema(example = "1")
    private long totalActiveCycles;
    @Schema(example = "1")
    private long totalClosedCycles;

    // Goals
    @Schema(example = "1")
    private long totalGoals;
    @Schema(example = "1")
    private long achievedGoals;
    @Schema(example = "75")
    private long inProgressGoals;
    @Schema(example = "1")
    private long missedGoals;
    @Schema(example = "100.00")
    private double goalCompletionRate;
    @Schema(example = "100.00")
    private double averageGoalProgress;

    // Reviews
    @Schema(example = "1")
    private long totalReviews;
    @Schema(example = "1")
    private long selfReviews;
    @Schema(example = "1")
    private long managerReviews;
    @Schema(example = "1")
    private long pendingReviews;
    @Schema(example = "1")
    private long finalizedReviews;
    @Schema(example = "100.00")
    private double averageRating;

    // PIPs
    @Schema(example = "1")
    private long totalPips;
    @Schema(example = "1")
    private long activePips;
    @Schema(example = "1")
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
