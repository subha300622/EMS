package com.example.ems.reports.subscription.projection;

public interface ChurnMetricsProjection {
    Double getChurnRate();
    Long getCancelledSubscriptions();
    Long getRenewedSubscriptions();
    Double getRetentionRate();
}
