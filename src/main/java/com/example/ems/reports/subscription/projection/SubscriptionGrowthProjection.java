package com.example.ems.reports.subscription.projection;

public interface SubscriptionGrowthProjection {
    String getPeriodLabel();
    Integer getNewSubscriptions();
    Integer getRenewals();
    Integer getCancellations();
}
