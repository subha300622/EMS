package com.example.ems.reports.subscription.dto;

public class PlanDistributionEntry {
    private String plan;
    private long organizationCount;
    private long organizations;
    private double percentage;

    public PlanDistributionEntry() {}

    public PlanDistributionEntry(String plan, long organizationCount, double percentage) {
        this.plan = plan;
        this.organizationCount = organizationCount;
        this.organizations = organizationCount;
        this.percentage = percentage;
    }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public long getOrganizationCount() { return organizationCount; }
    public void setOrganizationCount(long organizationCount) { 
        this.organizationCount = organizationCount; 
        this.organizations = organizationCount;
    }

    public long getOrganizations() { return organizations; }
    public void setOrganizations(long organizations) { 
        this.organizations = organizations; 
        this.organizationCount = organizations;
    }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
