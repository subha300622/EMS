package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface PlanRevenueDistributionProjection {
    String getPlanCode();
    Long getOrganizationCount();
    Long getSubscribers();
    BigDecimal getMonthlyRevenue();
    BigDecimal getAnnualRevenue();
    BigDecimal getLifetimeRevenue();
    BigDecimal getAverageRevenue();
    Double getGrowth();
}
