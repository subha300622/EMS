package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization statistics response")
public class OrganizationStatisticsResponse {
    @Schema(description = "Total Employees Count", example = "100")
    private long employees;

    @Schema(description = "Total Departments Count", example = "8")
    private long departments;

    @Schema(description = "Total Organization Admins Count", example = "2")
    private long admins;

    @Schema(description = "Total Active Users Count", example = "95")
    private long activeUsers;

    @Schema(description = "Monthly Revenue Generated", example = "499.0")
    private double monthlyRevenue;

    @Schema(description = "Cloud Storage Used in GB", example = "12.5")
    private double storageUsedGB;

    @Schema(description = "Last Login Timestamp", example = "2026-07-01T10:00:00Z")
    private String lastLogin;

    public OrganizationStatisticsResponse() {}

    public OrganizationStatisticsResponse(long employees, long departments, long admins, long activeUsers, double monthlyRevenue, double storageUsedGB, String lastLogin) {
        this.employees = employees;
        this.departments = departments;
        this.admins = admins;
        this.activeUsers = activeUsers;
        this.monthlyRevenue = monthlyRevenue;
        this.storageUsedGB = storageUsedGB;
        this.lastLogin = lastLogin;
    }

    public long getEmployees() { return employees; }
    public void setEmployees(long employees) { this.employees = employees; }

    public long getDepartments() { return departments; }
    public void setDepartments(long departments) { this.departments = departments; }

    public long getAdmins() { return admins; }
    public void setAdmins(long admins) { this.admins = admins; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public double getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(double monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public double getStorageUsedGB() { return storageUsedGB; }
    public void setStorageUsedGB(double storageUsedGB) { this.storageUsedGB = storageUsedGB; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
