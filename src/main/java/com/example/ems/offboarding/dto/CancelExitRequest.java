package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelExitRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
