package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateTicketResponse {

    @Schema(example = "102")
    private Long id;
    @Schema(example = "SUP-2026-000102")
    private String ticketNumber;
    @Schema(example = "Request for Leave")
    private String subject;
    @Schema(example = "Detailed description of the ticket")
    private String description;
    @Schema(example = "MEDIUM")
    private String priority;
    @Schema(example = "OPEN")
    private String status;
    @Schema(example = "2026-08-21T17:40:04Z")
    private String updatedAt;
    @Schema(example = "Ticket updated successfully")
    private String message;

    public UpdateTicketResponse() {}

    public UpdateTicketResponse(Long id, String ticketNumber, String subject, String description,
                                String priority, String status, String updatedAt, String message) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    public UpdateTicketResponse(Long id, String ticketNumber, String subject,
                                String priority, String status, String updatedAt, String message) {
        this(id, ticketNumber, subject, null, priority, status, updatedAt, message);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTicketId() { return id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
