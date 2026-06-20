package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CloseTicketResponse {
    @Schema(example = "1")
    private Long ticketId;
    @Schema(example = "string")
    private String ticketNumber;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String closedAt;
    @Schema(example = "1")
    private Integer rating;
    @Schema(example = "Excellent progress")
    private String feedback;
    @Schema(example = "string")
    private String message;

    public CloseTicketResponse() {}

    public CloseTicketResponse(Long ticketId, String ticketNumber, String status, String closedAt, Integer rating, String feedback, String message) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.status = status;
        this.closedAt = closedAt;
        this.rating = rating;
        this.feedback = feedback;
        this.message = message;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
