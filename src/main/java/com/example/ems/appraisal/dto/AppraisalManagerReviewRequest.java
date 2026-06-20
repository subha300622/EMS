package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppraisalManagerReviewRequest {

    @NotBlank(message = "Manager review comments are required")
    @Schema(example = "string")
    private String managerReview;

    @NotNull(message = "Manager rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(example = "1")
    private Integer managerRating;

    public String getManagerReview() { return managerReview; }
    public void setManagerReview(String managerReview) { this.managerReview = managerReview; }

    public Integer getManagerRating() { return managerRating; }
    public void setManagerRating(Integer managerRating) { this.managerRating = managerRating; }
}
