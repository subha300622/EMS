package com.example.ems.reports.subscription.projection;

public interface PlanDistributionProjection {
    String getPlanCode();
    Long getOrganizationCount();
    Double getPercentage();
}
