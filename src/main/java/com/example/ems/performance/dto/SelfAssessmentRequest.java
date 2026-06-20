package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class SelfAssessmentRequest {
    @Schema(example = "1")
    private Integer selfRating;
    @Schema(example = "string")
    private String selfReview;
    private List<String> achievements;
    private List<String> strengths;
    private List<String> improvementAreas;

    // Getters and Setters
    public Integer getSelfRating() { return selfRating; }
    public void setSelfRating(Integer selfRating) { this.selfRating = selfRating; }
    public String getSelfReview() { return selfReview; }
    public void setSelfReview(String selfReview) { this.selfReview = selfReview; }
    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }
    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }
    public List<String> getImprovementAreas() { return improvementAreas; }
    public void setImprovementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas; }
}
