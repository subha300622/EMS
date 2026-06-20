package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.performance.entity.PerformanceCycle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PerformanceCycleResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String name;
    @Schema(example = "2026-06-19")
    private LocalDate startDate;
    @Schema(example = "2026-06-19")
    private LocalDate endDate;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "1")
    private long durationDays;
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
    @Schema(example = "1")
    private long totalReviews;
    @Schema(example = "1")
    private long finalizedReviews;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
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
