package com.example.ems.organization.dto;

public class OrganizationSummaryResponse {
    private long totalOrganizations;
    private long activeOrganizations;
    private long suspendedOrganizations;
    private long trialOrganizations;
    private long premiumOrganizations;
    private long enterpriseOrganizations;

    public OrganizationSummaryResponse() {}

    public OrganizationSummaryResponse(long totalOrganizations, long activeOrganizations, long suspendedOrganizations, long trialOrganizations, long premiumOrganizations, long enterpriseOrganizations) {
        this.totalOrganizations = totalOrganizations;
        this.activeOrganizations = activeOrganizations;
        this.suspendedOrganizations = suspendedOrganizations;
        this.trialOrganizations = trialOrganizations;
        this.premiumOrganizations = premiumOrganizations;
        this.enterpriseOrganizations = enterpriseOrganizations;
    }

    public long getTotalOrganizations() { return totalOrganizations; }
    public void setTotalOrganizations(long totalOrganizations) { this.totalOrganizations = totalOrganizations; }

    public long getActiveOrganizations() { return activeOrganizations; }
    public void setActiveOrganizations(long activeOrganizations) { this.activeOrganizations = activeOrganizations; }

    public long getSuspendedOrganizations() { return suspendedOrganizations; }
    public void setSuspendedOrganizations(long suspendedOrganizations) { this.suspendedOrganizations = suspendedOrganizations; }

    public long getTrialOrganizations() { return trialOrganizations; }
    public void setTrialOrganizations(long trialOrganizations) { this.trialOrganizations = trialOrganizations; }

    public long getPremiumOrganizations() { return premiumOrganizations; }
    public void setPremiumOrganizations(long premiumOrganizations) { this.premiumOrganizations = premiumOrganizations; }

    public long getEnterpriseOrganizations() { return enterpriseOrganizations; }
    public void setEnterpriseOrganizations(long enterpriseOrganizations) { this.enterpriseOrganizations = enterpriseOrganizations; }
}
