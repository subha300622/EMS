package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppraisalSelfReviewRequest {

    @NotBlank(message = "Self-review comments are required")
    @Schema(example = "string")
    private String selfReview;

    @NotNull(message = "Self-rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(example = "1")
    private Integer selfRating;

    public String getSelfReview() { return selfReview; }
    public void setSelfReview(String selfReview) { this.selfReview = selfReview; }

    public Integer getSelfRating() { return selfRating; }
    public void setSelfRating(Integer selfRating) { this.selfRating = selfRating; }
}
