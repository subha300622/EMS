package com.example.ems.recruitment.dto;

import jakarta.validation.constraints.NotBlank;

public class CandidateRejectRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
