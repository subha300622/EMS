package com.example.ems.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class OnboardingDocumentVerifyRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
