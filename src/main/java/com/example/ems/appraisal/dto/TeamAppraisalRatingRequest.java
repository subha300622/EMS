package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record TeamAppraisalRatingRequest(
    @NotNull(message = "Manager rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Double managerRating,
    
    String managerComments
) {}
