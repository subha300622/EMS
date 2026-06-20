package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class EscalateTicketRequest {
    @Schema(example = "Personal business")
    private String escalationReason;

    public EscalateTicketRequest() {}

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
}
