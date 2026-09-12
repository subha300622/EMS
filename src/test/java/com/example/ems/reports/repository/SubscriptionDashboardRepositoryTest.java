package com.example.ems.reports.repository;

import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.reports.subscription.projection.*;
import com.example.ems.reports.subscription.repository.SubscriptionDashboardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SubscriptionDashboardRepositoryTest {

    @Autowired
    private SubscriptionDashboardRepository dashboardRepository;

    @Test
    public void testBasicCounts() {
        Long totalOrgs = dashboardRepository.countTotalOrganizations();
        assertThat(totalOrgs).isNotNull();

        Long activeCount = dashboardRepository.countByStatus(SubscriptionStatus.ACTIVE);
        assertThat(activeCount).isNotNull();
    }

    @Test
    public void testCalculateMonthlyRevenue() {
        BigDecimal revenue = dashboardRepository.calculateMonthlyRevenue();
        assertThat(revenue).isNotNull();
    }

    @Test
    public void testGetStatusDistribution() {
        List<SubscriptionStatusProjection> distribution = dashboardRepository.getStatusDistribution();
        assertThat(distribution).isNotNull();
    }

    @Test
    public void testGetPlanDistribution() {
        List<PlanDistributionProjection> distribution = dashboardRepository.getPlanDistribution();
        assertThat(distribution).isNotNull();
    }

    @Test
    public void testGetPlanRevenue() {
        List<PlanRevenueProjection> planRevenue = dashboardRepository.getPlanRevenue();
        assertThat(planRevenue).isNotNull();
    }

    @Test
    public void testGetGrowthTrend() {
        Instant now = Instant.now();
        Instant sixMonthsAgo = now.minus(180, ChronoUnit.DAYS);
        List<SubscriptionGrowthProjection> trend = dashboardRepository.getGrowthTrend(sixMonthsAgo, now, "YYYY-MM");
        assertThat(trend).isNotNull();
    }

    @Test
    public void testGetRevenueTrend() {
        Instant now = Instant.now();
        Instant sixMonthsAgo = now.minus(180, ChronoUnit.DAYS);
        List<RevenueTrendProjection> trend = dashboardRepository.getRevenueTrend(sixMonthsAgo, now, "YYYY-MM");
        assertThat(trend).isNotNull();
    }
}
