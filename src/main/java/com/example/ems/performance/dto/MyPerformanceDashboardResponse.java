package com.example.ems.performance.dto;

import java.util.List;

public class MyPerformanceDashboardResponse {
    private String employeeName;
    private String employeeId;
    private String designation;
    private String department;
    private Double overallRating;
    private Integer activeGoals;
    private Integer completedGoals;
    private Double goalCompletionPercentage;
    private String nextReviewDate;
    private List<RecentActivity> recentActivities;

    public static class RecentActivity {
        private String event;
        private String date;

        public RecentActivity(String event, String date) {
            this.event = event;
            this.date = date;
        }

        // Getters and Setters
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    // Getters and Setters
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }
    public Integer getActiveGoals() { return activeGoals; }
    public void setActiveGoals(Integer activeGoals) { this.activeGoals = activeGoals; }
    public Integer getCompletedGoals() { return completedGoals; }
    public void setCompletedGoals(Integer completedGoals) { this.completedGoals = completedGoals; }
    public Double getGoalCompletionPercentage() { return goalCompletionPercentage; }
    public void setGoalCompletionPercentage(Double goalCompletionPercentage) { this.goalCompletionPercentage = goalCompletionPercentage; }
    public String getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(String nextReviewDate) { this.nextReviewDate = nextReviewDate; }
    public List<RecentActivity> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivity> recentActivities) { this.recentActivities = recentActivities; }
}
