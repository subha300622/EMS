package com.example.ems.reports.subscription.dto;

import java.math.BigDecimal;

public class SubscriptionDashboardSummary {
    private long totalOrganizations;
    private long activeSubscriptions;
    private long trialSubscriptions;
    private long trialOrganizations;
    private long expiredSubscriptions;
    private long cancelledSubscriptions;
    private BigDecimal monthlyRevenue;
    private BigDecimal annualRevenue;
    private BigDecimal averageRevenuePerOrganization;

    public SubscriptionDashboardSummary() {}

    public SubscriptionDashboardSummary(long totalOrganizations, long activeSubscriptions, long trialSubscriptions, 
                                        long trialOrganizations, long expiredSubscriptions, long cancelledSubscriptions, 
                                        BigDecimal monthlyRevenue, BigDecimal annualRevenue, BigDecimal averageRevenuePerOrganization) {
        this.totalOrganizations = totalOrganizations;
        this.activeSubscriptions = activeSubscriptions;
        this.trialSubscriptions = trialSubscriptions;
        this.trialOrganizations = trialOrganizations;
        this.expiredSubscriptions = expiredSubscriptions;
        this.cancelledSubscriptions = cancelledSubscriptions;
        this.monthlyRevenue = monthlyRevenue;
        this.annualRevenue = annualRevenue;
        this.averageRevenuePerOrganization = averageRevenuePerOrganization;
    }

    public long getTotalOrganizations() { return totalOrganizations; }
    public void setTotalOrganizations(long totalOrganizations) { this.totalOrganizations = totalOrganizations; }

    public long getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(long activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }

    public long getTrialSubscriptions() { return trialSubscriptions; }
    public void setTrialSubscriptions(long trialSubscriptions) { this.trialSubscriptions = trialSubscriptions; }

    public long getTrialOrganizations() { return trialOrganizations; }
    public void setTrialOrganizations(long trialOrganizations) { this.trialOrganizations = trialOrganizations; }

    public long getExpiredSubscriptions() { return expiredSubscriptions; }
    public void setExpiredSubscriptions(long expiredSubscriptions) { this.expiredSubscriptions = expiredSubscriptions; }

    public long getCancelledSubscriptions() { return cancelledSubscriptions; }
    public void setCancelledSubscriptions(long cancelledSubscriptions) { this.cancelledSubscriptions = cancelledSubscriptions; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

    public BigDecimal getAverageRevenuePerOrganization() { return averageRevenuePerOrganization; }
    public void setAverageRevenuePerOrganization(BigDecimal averageRevenuePerOrganization) { this.averageRevenuePerOrganization = averageRevenuePerOrganization; }
}
