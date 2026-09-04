package com.example.ems.reports.service;

import com.example.ems.reports.subscription.dto.SubscriptionDashboardSummary;
import com.example.ems.reports.subscription.dto.SubscriptionStatusResponse;
import com.example.ems.reports.subscription.service.SubscriptionDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SubscriptionDashboardServiceTest {

    @Autowired
    private SubscriptionDashboardService dashboardService;

    @Test
    public void testGetSummary() {
        SubscriptionDashboardSummary summary = dashboardService.getSummary();
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalOrganizations()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getActiveSubscriptions()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testGetStatusDistribution() {
        SubscriptionStatusResponse dist = dashboardService.getStatusDistribution();
        assertThat(dist).isNotNull();
        assertThat(dist.getActive()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testGetConversion() {
        var conv = dashboardService.getConversion();
        assertThat(conv).isNotNull();
        assertThat(conv.getTrialOrganizations()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testGetChurn() {
        var churn = dashboardService.getChurn();
        assertThat(churn).isNotNull();
        assertThat(churn.getRetentionRate()).isBetween(0.0, 100.0);
    }
}
