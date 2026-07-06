package com.example.ems.reports.organization.dto;

import java.util.List;
import java.util.Map;

public class OrganizationReportDetail {
    private Long organizationId;
    private String organizationCode;
    private String organizationName;
    private String email;
    private String phone;
    private String website;
    private String createdDate;

    private String subscriptionPlan;
    private String status;
    private String subscriptionStartDate;
    private String subscriptionExpiryDate;

    private long organizationUserCount;
    private long activeUsers;
    private long departmentCount;
    private long roleCount;
    private double storageUsedGB;
    private double revenue;

    private List<String> modulesEnabled;
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
