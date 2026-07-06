package com.example.ems.reports.organization;

import com.example.ems.reports.organization.dto.ChartResponse;
import com.example.ems.reports.organization.dto.DashboardSummaryResponse;
import com.example.ems.reports.organization.dto.DistributionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DashboardFacade {

    @Autowired
    private OrganizationDashboardService dashboardService;

    public DashboardSummaryResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    public Map<String, ChartResponse> getGrowth() {
        return dashboardService.getGrowth();
    }

    public List<DistributionResponse> getStatusDistribution() {
        return dashboardService.getStatusDistribution();
    }

    public List<DistributionResponse> getSubscriptionDistribution() {
        return dashboardService.getSubscriptionDistribution();
    }

    public List<DistributionResponse> getEmployeeDistribution() {
        return dashboardService.getEmployeeDistribution();
    }

    public Map<String, Object> getActivityReport() {
        return dashboardService.getActivityReport();
    }
}
