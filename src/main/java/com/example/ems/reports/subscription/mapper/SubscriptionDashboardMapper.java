package com.example.ems.reports.subscription.mapper;

import com.example.ems.reports.subscription.dto.SubscriptionDashboardSummary;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SubscriptionDashboardMapper {

    public SubscriptionDashboardSummary toSummary(long totalOrgs, long active, long trialOrgs, long expired, long cancelled,
                                                  BigDecimal monthlyRevenue, BigDecimal annualRevenue, BigDecimal avgRevenue) {
        return new SubscriptionDashboardSummary(
                totalOrgs,
                active,
                trialOrgs, // trialSubscriptions
                trialOrgs, // trialOrganizations
                expired,
                cancelled,
                monthlyRevenue,
                annualRevenue,
                avgRevenue
        );
    }
}
