package com.example.ems.support.dto;

public class CloseTicketRequest {
    private Integer rating;
    private String feedback;

    public CloseTicketRequest() {}

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
