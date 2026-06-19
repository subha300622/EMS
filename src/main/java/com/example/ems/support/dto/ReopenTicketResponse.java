package com.example.ems.support.dto;

public class ReopenTicketResponse {

    private Long id;
    private String ticketNumber;
    private String previousStatus;
    private String status;
    private String reopenedAt;
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
