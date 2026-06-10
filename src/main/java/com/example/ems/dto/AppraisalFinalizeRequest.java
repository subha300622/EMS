package com.example.ems.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AppraisalFinalizeRequest {

    @NotNull(message = "Final rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer finalRating;

    public Integer getFinalRating() { return finalRating; }
    public void setFinalRating(Integer finalRating) { this.finalRating = finalRating; }
}
