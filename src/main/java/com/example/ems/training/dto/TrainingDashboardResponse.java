package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class TrainingDashboardResponse {
    @Schema(example = "1")
    private long totalCourses;
    @Schema(example = "1")
    private long activeCourses;
    @Schema(example = "1")
    private long totalSessions;
    @Schema(example = "1")
    private long totalEnrollments;
    @Schema(example = "1")
    private long completedEnrollments;
    @Schema(example = "100.00")
    private double averageProgress;
    @Schema(example = "100.00")
    private double withdrawalRate;

    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }

    public long getActiveCourses() { return activeCourses; }
    public void setActiveCourses(long activeCourses) { this.activeCourses = activeCourses; }

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(long totalEnrollments) { this.totalEnrollments = totalEnrollments; }

    public long getCompletedEnrollments() { return completedEnrollments; }
    public void setCompletedEnrollments(long completedEnrollments) { this.completedEnrollments = completedEnrollments; }

    public double getAverageProgress() { return averageProgress; }
    public void setAverageProgress(double averageProgress) { this.averageProgress = averageProgress; }

    public double getWithdrawalRate() { return withdrawalRate; }
    public void setWithdrawalRate(double withdrawalRate) { this.withdrawalRate = withdrawalRate; }
}
