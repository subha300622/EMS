package com.example.ems.finance.dto;

public class SendBackRequest {
    private String reason;

    public SendBackRequest() {}

    public SendBackRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
