package com.example.ems.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExitInterviewFeedbackRequest {

    @NotBlank(message = "Exit interview feedback narrative is required")
    private String feedback;

    @NotBlank(message = "Reason for leaving is required")
    private String reasonsForLeaving;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getReasonsForLeaving() { return reasonsForLeaving; }
    public void setReasonsForLeaving(String reasonsForLeaving) { this.reasonsForLeaving = reasonsForLeaving; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
