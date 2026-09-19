package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface RevenueByCurrencyProjection {
    String getCurrency();
    BigDecimal getTotalRevenue();
}
