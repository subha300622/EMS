package com.example.ems.reports.subscription.projection;

public interface ConversionMetricsProjection {
    Long getTrialOrganizations();
    Long getConvertedToPaid();
    Double getConversionRate();
    Double getAverageConversionDays();
}
