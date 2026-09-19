package com.example.ems.reports.subscription.projection;

import java.math.BigDecimal;

public interface RevenueTrendProjection {
    String getPeriod();
    BigDecimal getMonthlyRevenue();
    BigDecimal getAnnualRevenue();
    BigDecimal getTotalRevenue();
}
