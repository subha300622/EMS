package com.example.ems.training.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FeedbackSubmitRequest {

    @NotNull(message = "Rating is required")
    @Min(1) @Max(5)
    private Integer rating;

    @Min(1) @Max(5)
    private Integer contentQualityRating;

    @Min(1) @Max(5)
    private Integer trainerRating;

    @Min(1) @Max(5)
    private Integer overallExperienceRating;

    private String comments;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getContentQualityRating() { return contentQualityRating; }
    public void setContentQualityRating(Integer contentQualityRating) { this.contentQualityRating = contentQualityRating; }

    public Integer getTrainerRating() { return trainerRating; }
    public void setTrainerRating(Integer trainerRating) { this.trainerRating = trainerRating; }

    public Integer getOverallExperienceRating() { return overallExperienceRating; }
    public void setOverallExperienceRating(Integer overallExperienceRating) { this.overallExperienceRating = overallExperienceRating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
