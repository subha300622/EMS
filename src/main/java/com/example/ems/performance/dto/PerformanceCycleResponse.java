package com.example.ems.performance.dto;

import com.example.ems.performance.entity.PerformanceCycle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PerformanceCycleResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private long durationDays;
    private long totalGoals;
    private long achievedGoals;
    private long inProgressGoals;
    private long missedGoals;
    private double goalCompletionRate;
    private long totalReviews;
    private long finalizedReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PerformanceCycleResponse() {}

    public PerformanceCycleResponse(PerformanceCycle cycle) {
        this.id = cycle.getId();
        this.name = cycle.getName();
        this.startDate = cycle.getStartDate();
        this.endDate = cycle.getEndDate();
        this.status = cycle.getStatus();
        this.createdAt = cycle.getCreatedAt();
        this.updatedAt = cycle.getUpdatedAt();
        if (cycle.getStartDate() != null && cycle.getEndDate() != null) {
            this.durationDays = java.time.temporal.ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate());
        }
    }

    // Enriched via service after DB queries
    public void enrichGoalStats(long total, long achieved, long inProgress, long missed) {
        this.totalGoals = total;
        this.achievedGoals = achieved;
        this.inProgressGoals = inProgress;
        this.missedGoals = missed;
        this.goalCompletionRate = total > 0 ? Math.round(((double) achieved / total) * 100.0 * 100.0) / 100.0 : 0.0;
    }

    public void enrichReviewStats(long total, long finalized) {
        this.totalReviews = total;
        this.finalizedReviews = finalized;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getDurationDays() { return durationDays; }
    public void setDurationDays(long durationDays) { this.durationDays = durationDays; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
