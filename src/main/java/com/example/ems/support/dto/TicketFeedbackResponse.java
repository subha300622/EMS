package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class TicketFeedbackResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String ticketNumber;
    @Schema(example = "1")
    private Integer rating;
    @Schema(example = "Excellent progress")
    private String comment;
    @Schema(example = "string")
    private String submittedAt;
    @Schema(example = "string")
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
