package com.example.ems.organization.dto;

public class OrganizationStatisticsResponse {
    private long employees;
    private long departments;
    private long admins;
    private long activeUsers;
    private double monthlyRevenue;
    private double storageUsedGB;
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
