package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateGoalProgressRequest {
    
    // Existing fields for MyPerformanceService
    @Schema(example = "string")
    private String achievement;
    @Schema(example = "100.00")
    private Double progressPercentage;
    @Schema(example = "ACTIVE")
    private String status;

    // New fields for the Goals Module
    @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress cannot be more than 100")
    @Schema(example = "75")
    private Integer progress;

    @Schema(example = "Excellent progress")
    private String comment;

    // Getters and Setters
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
