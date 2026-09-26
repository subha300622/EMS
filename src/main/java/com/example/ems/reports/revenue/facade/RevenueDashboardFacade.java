package com.example.ems.reports.revenue.facade;

import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import com.example.ems.reports.revenue.dto.RevenueGrowthResponse;
import com.example.ems.reports.revenue.dto.RevenueSummaryResponse;
import com.example.ems.reports.revenue.dto.RevenueTrendResponse;
import com.example.ems.reports.revenue.service.RevenueDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RevenueDashboardFacade {

    @Autowired
    private RevenueDashboardService dashboardService;

    public void refreshMaterializedViews() {
        dashboardService.refreshMaterializedViews();
    }

    public RevenueSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    public List<RevenueTrendResponse> getTrends() {
        return dashboardService.getTrends();
    }

    public List<RevenueGrowthResponse> getGrowth() {
        return dashboardService.getGrowth();
    }

    public RevenueForecastResponse getForecast(int horizon) {
        return dashboardService.getForecast(horizon);
    }
}
