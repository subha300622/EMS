package com.example.ems.reports.service;

import com.example.ems.reports.subscription.dto.SubscriptionReportFilterRequest;
import com.example.ems.reports.subscription.service.SubscriptionAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Transactional
public class SubscriptionAnalyticsServiceTest {

    @Autowired
    private SubscriptionAnalyticsService analyticsService;

    @Test
    public void testGetSubscriptionList() {
        SubscriptionReportFilterRequest filter = new SubscriptionReportFilterRequest();
        filter.setPage(0);
        filter.setSize(10);
        filter.setSortBy("organizationId");
        filter.setDirection("asc");

        var list = analyticsService.getSubscriptionList(filter);
        assertThat(list).isNotNull();
        assertThat(list.getContent()).isNotNull();
    }

    @Test
    public void testGetExpiringSubscriptions() {
        var page = analyticsService.getExpiringSubscriptions(30, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    @Test
    public void testGetTrialOrganizations() {
        var page = analyticsService.getTrialOrganizations(PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }
}
