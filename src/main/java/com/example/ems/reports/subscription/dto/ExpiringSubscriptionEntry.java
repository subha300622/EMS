package com.example.ems.reports.subscription.dto;

public class ExpiringSubscriptionEntry {
    private Long organizationId;
    private String organizationName;
    private String plan;
    private String expiryDate;
    private long daysRemaining;

    public ExpiringSubscriptionEntry() {}

    public ExpiringSubscriptionEntry(Long organizationId, String organizationName, String plan, String expiryDate, long daysRemaining) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.plan = plan;
        this.expiryDate = expiryDate;
        this.daysRemaining = daysRemaining;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
}
