package com.example.ems.organization.dto;

import java.math.BigDecimal;
import java.util.List;

public class SubscriptionAnalyticsDtos {

    public record MRRMetric(
        BigDecimal value,
        String currency,
        double trend
    ) {}

    public record ActiveSubscriptionsMetric(
        int count,
        int delta
    ) {}

    public record RevenueCollectedMetric(
        BigDecimal value,
        double trend
    ) {}

    public record OverdueInvoicesMetric(
        BigDecimal value
    ) {}

    public record PlanDistributionEntry(
        String plan,
        int orgs,
        BigDecimal mrr
    ) {}

    public record UpcomingRenewalEntry(
        String organizationName,
        String plan,
        String billingCycle,
        BigDecimal amount,
        String renewalDate,
        int daysLeft
    ) {}

    public record RatioMetrics(
        double avgRevenuePerOrg,
        double churnRate,
        double paymentSuccessRate,
        BigDecimal pendingInvoicesValue
    ) {}

    public record SubscriptionOverviewDto(
        MRRMetric mrr,
        ActiveSubscriptionsMetric activeSubscriptions,
        RevenueCollectedMetric revenueCollected,
        OverdueInvoicesMetric overdueInvoices,
        int totalSubscribers,
        List<PlanDistributionEntry> planDistribution,
        List<UpcomingRenewalEntry> upcomingRenewals,
        RatioMetrics ratios
    ) {}
}
