package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;

public class SupportTicketEscalateRequest {

    @NotBlank(message = "Escalation reason is required")
    private String reason;

    public SupportTicketEscalateRequest() {}

    public SupportTicketEscalateRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
