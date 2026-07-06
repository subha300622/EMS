package com.example.ems.reports.subscription.projection;

import java.math.BigDecimal;

public interface PlanRevenueProjection {
    String getPlanCode();
    BigDecimal getRevenue();
}
