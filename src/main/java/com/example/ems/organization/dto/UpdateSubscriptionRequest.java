package com.example.ems.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class UpdateSubscriptionRequest {
    @NotBlank(message = "Plan is required")
    private String plan;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    public UpdateSubscriptionRequest() {}

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
