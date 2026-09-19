package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface RefundReasonProjection {
    String getRefundReason();
    Long getRefundCount();
    BigDecimal getTotalRefunded();
}
