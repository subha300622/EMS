package com.example.ems.performance.dto;

public class UpdateGoalProgressRequest {
    private String achievement;
    private Double progressPercentage;
    private String status;

    // Getters and Setters
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
