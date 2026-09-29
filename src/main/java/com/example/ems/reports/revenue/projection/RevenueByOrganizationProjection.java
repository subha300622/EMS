package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface RevenueByOrganizationProjection {
    Long getOrganizationId();
    String getOrganizationName();
    BigDecimal getTotalRevenue();
}
