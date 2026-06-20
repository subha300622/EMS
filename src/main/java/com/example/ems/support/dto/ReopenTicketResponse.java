package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ReopenTicketResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String ticketNumber;
    @Schema(example = "ACTIVE")
    private String previousStatus;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String reopenedAt;
    @Schema(example = "string")
    private String message;

    public ReopenTicketResponse() {}

    public ReopenTicketResponse(Long id, String ticketNumber, String previousStatus,
                                String status, String reopenedAt, String message) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.previousStatus = previousStatus;
        this.status = status;
        this.reopenedAt = reopenedAt;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(String reopenedAt) { this.reopenedAt = reopenedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
