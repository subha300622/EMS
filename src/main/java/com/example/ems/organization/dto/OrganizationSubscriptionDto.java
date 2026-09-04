package com.example.ems.organization.dto;

public class OrganizationSubscriptionDto {
    private String plan;
    private String status;
    private String startDate;
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
