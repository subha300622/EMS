package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface GatewayRevenueProjection {
    String getGateway();
    String getStatus();
    Long getPaymentCount();
    BigDecimal getTotalVolume();
}
