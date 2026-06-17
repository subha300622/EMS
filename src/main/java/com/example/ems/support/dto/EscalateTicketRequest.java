package com.example.ems.support.dto;

public class EscalateTicketRequest {
    private String escalationReason;

    public EscalateTicketRequest() {}

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
}
