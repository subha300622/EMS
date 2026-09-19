package com.example.ems.reports.revenue.projection;

import java.math.BigDecimal;

public interface OutstandingInvoiceProjection {
    Long getOrganizationId();
    String getOrganizationName();
    BigDecimal getOutstandingAmount();
}
