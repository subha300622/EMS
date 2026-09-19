package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization report list item response")
public class OrganizationReportListItem {
    @Schema(description = "Organization ID", example = "1")
    private Long organizationId;

    @Schema(description = "Organization Code", example = "ORG-1001")
    private String organizationCode;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String organizationName;

    @Schema(description = "Contact Email", example = "contact@acme.com")
    private String email;

    @Schema(description = "Organization Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Subscription Plan Name", example = "ENTERPRISE")
    private String subscriptionPlan;

    @Schema(description = "Total User Count", example = "100")
    private long organizationUserCount;

    @Schema(description = "Active User Count", example = "85")
    private long activeUsers;

    @Schema(description = "Registration Date", example = "2026-01-01")
    private String createdDate;

    public OrganizationReportListItem() {}

    public OrganizationReportListItem(Long organizationId, String organizationCode, String organizationName, String email, String status, String subscriptionPlan, long organizationUserCount, long activeUsers, String createdDate) {
        this.organizationId = organizationId;
        this.organizationCode = organizationCode;
        this.organizationName = organizationName;
        this.email = email;
        this.status = status;
        this.subscriptionPlan = subscriptionPlan;
        this.organizationUserCount = organizationUserCount;
        this.activeUsers = activeUsers;
        this.createdDate = createdDate;
    }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public long getOrganizationUserCount() { return organizationUserCount; }
    public void setOrganizationUserCount(long organizationUserCount) { this.organizationUserCount = organizationUserCount; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
