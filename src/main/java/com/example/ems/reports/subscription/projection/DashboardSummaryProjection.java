package com.example.ems.reports.subscription.projection;

import java.math.BigDecimal;

public interface DashboardSummaryProjection {
    Long getTotalOrganizations();
    Long getActiveSubscriptions();
    Long getTrialSubscriptions();
    Long getExpiredSubscriptions();
    Long getCancelledSubscriptions();
    BigDecimal getMonthlyRevenue();
    BigDecimal getAnnualRevenue();
}
