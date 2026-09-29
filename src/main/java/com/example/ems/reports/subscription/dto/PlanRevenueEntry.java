package com.example.ems.reports.subscription.dto;

import java.math.BigDecimal;

public class PlanRevenueEntry {
    private String planName;
    private long organizationCount;
    private BigDecimal monthlyRevenue;

    public PlanRevenueEntry() {}

    public PlanRevenueEntry(String planName, long organizationCount, BigDecimal monthlyRevenue) {
        this.planName = planName;
        this.organizationCount = organizationCount;
        this.monthlyRevenue = monthlyRevenue;
    }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public long getOrganizationCount() { return organizationCount; }
    public void setOrganizationCount(long organizationCount) { this.organizationCount = organizationCount; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
}
