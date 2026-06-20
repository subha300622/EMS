package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AppraisalFinalizeRequest {

    @NotNull(message = "Final rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(example = "1")
    private Integer finalRating;

    public Integer getFinalRating() { return finalRating; }
    public void setFinalRating(Integer finalRating) { this.finalRating = finalRating; }
}
