package com.example.ems.support.dto;

public class CreateTicketResponse {
    private Long ticketId;
    private String ticketNumber;
    private String status;
    private String priority;
    private String createdAt;
    private String expectedResponseAt;
    private String expectedResolutionAt;
    private String message;

    public CreateTicketResponse() {}

    public CreateTicketResponse(Long ticketId, String ticketNumber, String status, String priority, String createdAt, String expectedResponseAt, String expectedResolutionAt, String message) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.expectedResponseAt = expectedResponseAt;
        this.expectedResolutionAt = expectedResolutionAt;
        this.message = message;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getExpectedResponseAt() { return expectedResponseAt; }
    public void setExpectedResponseAt(String expectedResponseAt) { this.expectedResponseAt = expectedResponseAt; }

    public String getExpectedResolutionAt() { return expectedResolutionAt; }
    public void setExpectedResolutionAt(String expectedResolutionAt) { this.expectedResolutionAt = expectedResolutionAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
