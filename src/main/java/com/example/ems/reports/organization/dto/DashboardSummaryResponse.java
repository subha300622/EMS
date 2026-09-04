package com.example.ems.reports.organization.dto;

public class DashboardSummaryResponse {
    private long totalOrganizations;
    private long activeOrganizations;
    private long trialOrganizations;
    private long suspendedOrganizations;
    private long totalEmployees;
    private long activeUsers;
    private double monthlyGrowth;
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
