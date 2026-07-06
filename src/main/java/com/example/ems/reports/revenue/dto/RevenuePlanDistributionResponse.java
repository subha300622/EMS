package com.example.ems.reports.revenue.dto;

import java.math.BigDecimal;

public class RevenuePlanDistributionResponse {
    private String plan;
    private Long organizations;
    private Long subscribers;
    private BigDecimal monthlyRevenue;
    private BigDecimal annualRevenue;
    private BigDecimal lifetimeRevenue;
    private BigDecimal averageRevenue;
    private Double growth;

    public RevenuePlanDistributionResponse() {}

    public RevenuePlanDistributionResponse(String plan, Long organizations, Long subscribers, BigDecimal monthlyRevenue, BigDecimal annualRevenue, BigDecimal lifetimeRevenue, BigDecimal averageRevenue, Double growth) {
        this.plan = plan;
        this.organizations = organizations;
        this.subscribers = subscribers;
        this.monthlyRevenue = monthlyRevenue;
        this.annualRevenue = annualRevenue;
        this.lifetimeRevenue = lifetimeRevenue;
        this.averageRevenue = averageRevenue;
        this.growth = growth;
    }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Long getOrganizations() { return organizations; }
    public void setOrganizations(Long organizations) { this.organizations = organizations; }

    public Long getSubscribers() { return subscribers; }
    public void setSubscribers(Long subscribers) { this.subscribers = subscribers; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }

    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

    public BigDecimal getLifetimeRevenue() { return lifetimeRevenue; }
    public void setLifetimeRevenue(BigDecimal lifetimeRevenue) { this.lifetimeRevenue = lifetimeRevenue; }

    public BigDecimal getAverageRevenue() { return averageRevenue; }
    public void setAverageRevenue(BigDecimal averageRevenue) { this.averageRevenue = averageRevenue; }

    public Double getGrowth() { return growth; }
    public void setGrowth(Double growth) { this.growth = growth; }
}
