package com.example.ems.support.dto;

public class EscalateTicketResponse {
    private Long ticketId;
    private String ticketNumber;
    private String previousPriority;
    private String currentPriority;
    private String escalatedAt;
    private String message;

    public EscalateTicketResponse() {}

    public EscalateTicketResponse(Long ticketId, String ticketNumber, String previousPriority, String currentPriority, String escalatedAt, String message) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.previousPriority = previousPriority;
        this.currentPriority = currentPriority;
        this.escalatedAt = escalatedAt;
        this.message = message;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getPreviousPriority() { return previousPriority; }
    public void setPreviousPriority(String previousPriority) { this.previousPriority = previousPriority; }

    public String getCurrentPriority() { return currentPriority; }
    public void setCurrentPriority(String currentPriority) { this.currentPriority = currentPriority; }

    public String getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(String escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
