package com.example.ems.support.dto;

public class TicketFeedbackResponse {

    private Long id;
    private String ticketNumber;
    private Integer rating;
    private String comment;
    private String submittedAt;
    private String message;

    public TicketFeedbackResponse() {}

    public TicketFeedbackResponse(Long id, String ticketNumber, Integer rating,
                                  String comment, String submittedAt, String message) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.rating = rating;
        this.comment = comment;
        this.submittedAt = submittedAt;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
