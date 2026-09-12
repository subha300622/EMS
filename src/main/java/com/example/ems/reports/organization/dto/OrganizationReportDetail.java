package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Detailed organization analytical report response")
public class OrganizationReportDetail {
    @Schema(description = "Organization Database ID", example = "1")
    private Long organizationId;

    @Schema(description = "Organization Code", example = "ORG-1001")
    private String organizationCode;

    @Schema(description = "Organization Name", example = "Acme Corp")
    private String organizationName;

    @Schema(description = "Contact Email", example = "contact@acme.com")
    private String email;

    @Schema(description = "Contact Phone", example = "+1-555-0199")
    private String phone;

    @Schema(description = "Website URL", example = "https://acme.com")
    private String website;

    @Schema(description = "Created Date", example = "2026-01-01")
    private String createdDate;

    @Schema(description = "Subscription Plan Name", example = "ENTERPRISE")
    private String subscriptionPlan;

    @Schema(description = "Subscription Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Subscription Start Date", example = "2026-01-01")
    private String subscriptionStartDate;

    @Schema(description = "Subscription Expiry Date", example = "2027-01-01")
    private String subscriptionExpiryDate;

    @Schema(description = "Total Users Count", example = "100")
    private long organizationUserCount;

    @Schema(description = "Active Users Count", example = "85")
    private long activeUsers;

    @Schema(description = "Departments Count", example = "8")
    private long departmentCount;

    @Schema(description = "Roles Defined Count", example = "5")
    private long roleCount;

    @Schema(description = "Storage Used in GB", example = "15.0")
    private double storageUsedGB;

    @Schema(description = "Revenue Generated", example = "350.0")
    private double revenue;

    @Schema(description = "Enabled Modules List", example = "[\"HR\", \"FINANCE\", \"ATTENDANCE\"]")
    private List<String> modulesEnabled;

    @Schema(description = "Audit Summary Statistics")
    private Map<String, Object> auditSummary;

    public OrganizationReportDetail() {}

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubscriptionStartDate() { return subscriptionStartDate; }
    public void setSubscriptionStartDate(String subscriptionStartDate) { this.subscriptionStartDate = subscriptionStartDate; }

    public String getSubscriptionExpiryDate() { return subscriptionExpiryDate; }
    public void setSubscriptionExpiryDate(String subscriptionExpiryDate) { this.subscriptionExpiryDate = subscriptionExpiryDate; }

    public long getOrganizationUserCount() { return organizationUserCount; }
    public void setOrganizationUserCount(long organizationUserCount) { this.organizationUserCount = organizationUserCount; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getDepartmentCount() { return departmentCount; }
    public void setDepartmentCount(long departmentCount) { this.departmentCount = departmentCount; }

    public long getRoleCount() { return roleCount; }
    public void setRoleCount(long roleCount) { this.roleCount = roleCount; }

    public double getStorageUsedGB() { return storageUsedGB; }
    public void setStorageUsedGB(double storageUsedGB) { this.storageUsedGB = storageUsedGB; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public List<String> getModulesEnabled() { return modulesEnabled; }
    public void setModulesEnabled(List<String> modulesEnabled) { this.modulesEnabled = modulesEnabled; }

    public Map<String, Object> getAuditSummary() { return auditSummary; }
    public void setAuditSummary(Map<String, Object> auditSummary) { this.auditSummary = auditSummary; }
}
