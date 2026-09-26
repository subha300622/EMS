package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization subscription summary")
public class OrganizationSubscriptionDto {
    @Schema(description = "Plan name/tier", example = "ENTERPRISE")
    private String plan;

    @Schema(description = "Subscription Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Subscription Start Date", example = "2026-01-01")
    private String startDate;

    @Schema(description = "Subscription Expiry Date", example = "2027-01-01")
    private String expiryDate;

    public OrganizationSubscriptionDto() {}

    public OrganizationSubscriptionDto(String plan, String status, String startDate, String expiryDate) {
        this.plan = plan;
        this.status = status;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
    }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
