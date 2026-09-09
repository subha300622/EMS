package com.example.ems.reports.revenue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Plan revenue distribution response")
public class RevenuePlanDistributionResponse {
    @Schema(description = "Plan Name", example = "ENTERPRISE")
    private String plan;

    @Schema(description = "Number of Organizations on this Plan", example = "20")
    private Long organizations;

    @Schema(description = "Number of Subscribers", example = "1200")
    private Long subscribers;

    @Schema(description = "Monthly Revenue Generated", example = "45000.00")
    private BigDecimal monthlyRevenue;

    @Schema(description = "Annual Revenue Generated", example = "540000.00")
    private BigDecimal annualRevenue;

    @Schema(description = "Lifetime Revenue Generated", example = "1200000.00")
    private BigDecimal lifetimeRevenue;

    @Schema(description = "Average Revenue Per Account", example = "2250.00")
    private BigDecimal averageRevenue;

    @Schema(description = "Plan Growth Percentage", example = "15.4")
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
