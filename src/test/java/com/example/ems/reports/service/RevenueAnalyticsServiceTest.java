package com.example.ems.reports.service;

import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import com.example.ems.reports.revenue.service.RevenueAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenueAnalyticsServiceTest {

    @Autowired
    private RevenueAnalyticsService analyticsService;

    @Test
    public void testGetPaymentsReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);
        filters.setSortBy("id");
        filters.setDirection("desc");

        var page = analyticsService.getPaymentsReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void testGetInvoicesReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);

        var page = analyticsService.getInvoicesReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void testGetRefundsReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);

        var page = analyticsService.getRefundsReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void testGetPlansReport() {
        var plans = analyticsService.getPlansReport();
        assertThat(plans).isNotNull();
    }
}
