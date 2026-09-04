package com.example.ems.reports.organization.dto;

public class OrganizationReportListItem {
    private Long organizationId;
    private String organizationCode;
    private String organizationName;
    private String email;
    private String status;
    private String subscriptionPlan;
    private long organizationUserCount;
    private long activeUsers;
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
