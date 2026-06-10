package com.example.ems.dto;

public class TrainingDashboardResponse {
    private long totalCourses;
    private long activeCourses;
    private long totalSessions;
    private long totalEnrollments;
    private long completedEnrollments;
    private double averageProgress;
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
