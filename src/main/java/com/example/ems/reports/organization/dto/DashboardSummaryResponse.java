package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform organization dashboard summary metrics")
public class DashboardSummaryResponse {
    @Schema(description = "Total Organizations Count", example = "50")
    private long totalOrganizations;

    @Schema(description = "Active Organizations Count", example = "42")
    private long activeOrganizations;

    @Schema(description = "Trial Organizations Count", example = "5")
    private long trialOrganizations;

    @Schema(description = "Suspended Organizations Count", example = "3")
    private long suspendedOrganizations;

    @Schema(description = "Total Employees across all organizations", example = "1500")
    private long totalEmployees;

    @Schema(description = "Total Active Users", example = "1320")
    private long activeUsers;

    @Schema(description = "Month-over-month growth percentage", example = "12.4")
    private double monthlyGrowth;

    @Schema(description = "Total storage used across platform in GB", example = "812.0")
    private double storageUsedGB;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(long totalOrganizations, long activeOrganizations, long trialOrganizations, long suspendedOrganizations, long totalEmployees, long activeUsers, double monthlyGrowth, double storageUsedGB) {
        this.totalOrganizations = totalOrganizations;
        this.activeOrganizations = activeOrganizations;
        this.trialOrganizations = trialOrganizations;
        this.suspendedOrganizations = suspendedOrganizations;
        this.totalEmployees = totalEmployees;
        this.activeUsers = activeUsers;
        this.monthlyGrowth = monthlyGrowth;
        this.storageUsedGB = storageUsedGB;
    }

    public long getTotalOrganizations() { return totalOrganizations; }
    public void setTotalOrganizations(long totalOrganizations) { this.totalOrganizations = totalOrganizations; }

    public long getActiveOrganizations() { return activeOrganizations; }
    public void setActiveOrganizations(long activeOrganizations) { this.activeOrganizations = activeOrganizations; }

    public long getTrialOrganizations() { return trialOrganizations; }
    public void setTrialOrganizations(long trialOrganizations) { this.trialOrganizations = trialOrganizations; }

    public long getSuspendedOrganizations() { return suspendedOrganizations; }
    public void setSuspendedOrganizations(long suspendedOrganizations) { this.suspendedOrganizations = suspendedOrganizations; }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public double getMonthlyGrowth() { return monthlyGrowth; }
    public void setMonthlyGrowth(double monthlyGrowth) { this.monthlyGrowth = monthlyGrowth; }

    public double getStorageUsedGB() { return storageUsedGB; }
    public void setStorageUsedGB(double storageUsedGB) { this.storageUsedGB = storageUsedGB; }
}
