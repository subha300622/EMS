package com.example.ems.reports.subscription.facade;

import com.example.ems.reports.subscription.dto.*;
import com.example.ems.reports.subscription.service.SubscriptionDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SubscriptionDashboardFacade {

    @Autowired
    private SubscriptionDashboardService dashboardService;

    public SubscriptionDashboardSummary getSummary() {
        return dashboardService.getSummary();
    }

    public List<SubscriptionGrowthEntry> getGrowth(String period, LocalDate from, LocalDate to) {
        return dashboardService.getGrowth(period, from, to);
    }

    public SubscriptionStatusResponse getStatusDistribution() {
        return dashboardService.getStatusDistribution();
    }

    public List<RevenueReportEntry> getRevenueReport(String period, LocalDate from, LocalDate to) {
        return dashboardService.getRevenueReport(period, from, to);
    }

    public List<PlanRevenueEntry> getPlanRevenue() {
        return dashboardService.getPlanRevenue();
    }

    public List<PlanDistributionEntry> getPlanDistribution() {
        return dashboardService.getPlanDistribution();
    }

    public SubscriptionConversionResponse getConversion() {
        return dashboardService.getConversion();
    }

    public SubscriptionChurnResponse getChurn() {
        return dashboardService.getChurn();
    }
}
