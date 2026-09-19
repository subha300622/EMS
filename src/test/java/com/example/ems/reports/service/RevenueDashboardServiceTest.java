package com.example.ems.reports.service;

import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import com.example.ems.reports.revenue.dto.RevenueSummaryResponse;
import com.example.ems.reports.revenue.service.RevenueDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenueDashboardServiceTest {

    @Autowired
    private RevenueDashboardService dashboardService;

    @Test
    public void testGetSummary() {
        dashboardService.refreshMaterializedViews();
        RevenueSummaryResponse summary = dashboardService.getSummary();
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalRevenue()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void testGetTrends() {
        dashboardService.refreshMaterializedViews();
        var trends = dashboardService.getTrends();
        assertThat(trends).isNotNull();
    }

    @Test
    public void testGetGrowth() {
        dashboardService.refreshMaterializedViews();
        var growth = dashboardService.getGrowth();
        assertThat(growth).isNotNull();
    }

    @Test
    public void testGetForecast() {
        RevenueForecastResponse forecast = dashboardService.getForecast(6);
        assertThat(forecast).isNotNull();
        assertThat(forecast.getHorizonMonths()).isEqualTo(6);
        assertThat(forecast.getForecastConfidenceScore()).isBetween(0.0, 100.0);
    }
}
