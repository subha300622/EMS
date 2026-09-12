package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform-wide organizations summary response")
public class OrganizationSummaryResponse {
    @Schema(description = "Total Organizations Count", example = "50")
    private long totalOrganizations;

    @Schema(description = "Active Organizations Count", example = "42")
    private long activeOrganizations;

    @Schema(description = "Suspended Organizations Count", example = "3")
    private long suspendedOrganizations;

    @Schema(description = "Trial Organizations Count", example = "5")
    private long trialOrganizations;

    @Schema(description = "Premium Tier Organizations Count", example = "25")
    private long premiumOrganizations;

    @Schema(description = "Enterprise Tier Organizations Count", example = "17")
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
