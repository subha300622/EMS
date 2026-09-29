package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    String getPeriod();
    BigDecimal getGrossRevenue();
    BigDecimal getNetRevenue();
    BigDecimal getRefundAmount();
    BigDecimal getTaxCollected();
    BigDecimal getDiscountAmount();
    Long getSuccessfulPayments();
    Long getFailedPayments();
}
