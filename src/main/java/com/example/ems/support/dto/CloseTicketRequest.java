package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CloseTicketRequest {
    @Schema(example = "1")
    private Integer rating;
    @Schema(example = "Excellent progress")
    private String feedback;

    public CloseTicketRequest() {}

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
