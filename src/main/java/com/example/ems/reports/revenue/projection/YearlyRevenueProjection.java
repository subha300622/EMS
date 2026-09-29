package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface YearlyRevenueProjection {
    String getYear();
    BigDecimal getGrossRevenue();
    BigDecimal getNetRevenue();
    BigDecimal getRefundAmount();
}
