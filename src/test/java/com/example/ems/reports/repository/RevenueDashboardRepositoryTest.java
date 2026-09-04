package com.example.ems.reports.repository;

import com.example.ems.reports.revenue.repository.RevenueDashboardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenueDashboardRepositoryTest {

    @Autowired
    private RevenueDashboardRepository dashboardRepository;

    @Test
    public void testOverallTotals() {
        dashboardRepository.refreshDailyView();
        var totals = dashboardRepository.getOverallDashboardTotals();
        assertThat(totals).isNotNull();
        assertThat(totals.isEmpty()).isFalse();
    }

    @Test
    public void testCalculateActiveMrr() {
        BigDecimal mrr = dashboardRepository.calculateActiveMrr();
        assertThat(mrr).isNotNull();
        assertThat(mrr).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void testGetMonthlyRevenueTrends() {
        dashboardRepository.refreshMonthlyView();
        var trends = dashboardRepository.getMonthlyRevenueTrends();
        assertThat(trends).isNotNull();
    }

    @Test
    public void testGetPlanRevenueDistribution() {
        var distribution = dashboardRepository.getPlanRevenueDistribution();
        assertThat(distribution).isNotNull();
    }

    @Test
    public void testGetRefundReasonsDistribution() {
        var refunds = dashboardRepository.getRefundReasonsDistribution();
        assertThat(refunds).isNotNull();
    }

    @Test
    public void testGetTopCustomers() {
        var topCustomers = dashboardRepository.getTopCustomers();
        assertThat(topCustomers).isNotNull();
    }
}
