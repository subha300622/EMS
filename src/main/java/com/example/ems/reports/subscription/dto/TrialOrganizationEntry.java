package com.example.ems.reports.subscription.dto;

public class TrialOrganizationEntry {
    private Long organizationId;
    private String organizationName;
    private String trialStartDate;
    private String trialEndDate;
    private long daysRemaining;

    public TrialOrganizationEntry() {}

    public TrialOrganizationEntry(Long organizationId, String organizationName, String trialStartDate, String trialEndDate, long daysRemaining) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.trialStartDate = trialStartDate;
        this.trialEndDate = trialEndDate;
        this.daysRemaining = daysRemaining;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(String trialStartDate) { this.trialStartDate = trialStartDate; }

    public String getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(String trialEndDate) { this.trialEndDate = trialEndDate; }

    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }
}
