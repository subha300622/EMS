package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface TopCustomerRevenueProjection {
    Long getOrganizationId();
    String getOrganizationName();
    BigDecimal getTotalRevenue();
    Long getPaymentCount();
}
