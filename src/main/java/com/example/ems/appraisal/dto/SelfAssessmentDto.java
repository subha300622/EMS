package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SelfAssessmentDto {

    @NotNull(message = "overallRating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private Double overallRating;

    private String strengths;
    private String achievements;
    private String developmentAreas;
    private LocalDateTime submittedAt;

    public SelfAssessmentDto() {}

    public SelfAssessmentDto(Double overallRating, String strengths, String achievements, String developmentAreas, LocalDateTime submittedAt) {
        this.overallRating = overallRating;
        this.strengths = strengths;
        this.achievements = achievements;
        this.developmentAreas = developmentAreas;
        this.submittedAt = submittedAt;
    }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getDevelopmentAreas() { return developmentAreas; }
    public void setDevelopmentAreas(String developmentAreas) { this.developmentAreas = developmentAreas; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
