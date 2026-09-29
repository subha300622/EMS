package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;

public class SupportTicketPriorityUpdateRequest {

    @NotBlank(message = "priority is required (CRITICAL, HIGH, MEDIUM, LOW)")
    private String priority;

    @NotBlank(message = "Reason is required when changing priority")
    private String reason;

    public SupportTicketPriorityUpdateRequest() {}

    public SupportTicketPriorityUpdateRequest(String priority, String reason) {
        this.priority = priority;
        this.reason = reason;
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
