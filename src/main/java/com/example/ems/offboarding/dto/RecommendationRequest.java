package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecommendationRequest {
    @NotNull(message = "Rating is required")
    @Min(value = 0, message = "Rating must be at least 0.0")
    @Max(value = 5, message = "Rating must be at most 5.0")
    private Double rating;

    @NotBlank(message = "Recommendation text is required")
    private String recommendation;

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
