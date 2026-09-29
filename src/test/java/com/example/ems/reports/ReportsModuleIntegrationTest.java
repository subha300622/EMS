package com.example.ems.reports;

import com.example.ems.reports.revenue.dto.RevenueExportRequest;
import com.example.ems.reports.revenue.dto.RevenueFilterRequest;
import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import com.example.ems.reports.revenue.dto.RevenueSummaryResponse;
import com.example.ems.reports.revenue.repository.RevenueInvoiceRepository;
import com.example.ems.reports.revenue.repository.RevenuePaymentRepository;
import com.example.ems.reports.revenue.repository.RevenueDashboardRepository;
import com.example.ems.reports.revenue.service.RevenueAnalyticsService;
import com.example.ems.reports.revenue.service.RevenueDashboardService;
import com.example.ems.reports.revenue.service.RevenueReportExportService;
import com.example.ems.reports.revenue.specification.InvoiceSpecification;
import com.example.ems.reports.revenue.specification.PaymentSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consolidated integration test for the Reports Revenue module (19 tests).
 *
 * Sections:
 *   - Revenue analytics
 *   - Revenue dashboard
 *   - Revenue exports
 *   - Revenue invoice repository
 *   - Revenue payment repository
 *   - Revenue dashboard repository
 */
@SpringBootTest
@Transactional
public class ReportsModuleIntegrationTest {

    // -------------------------------------------------------------------------
    // Revenue analytics
    // -------------------------------------------------------------------------

    @Autowired private RevenueAnalyticsService revenueAnalyticsService;

    @Test
    public void revenue_analytics_getPaymentsReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);
        filters.setSortBy("id");
        filters.setDirection("desc");

        var page = revenueAnalyticsService.getPaymentsReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void revenue_analytics_getInvoicesReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);

        var page = revenueAnalyticsService.getInvoicesReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void revenue_analytics_getRefundsReport() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setPage(0);
        filters.setSize(10);

        var page = revenueAnalyticsService.getRefundsReport(filters);
        assertThat(page).isNotNull();
    }

    @Test
    public void revenue_analytics_getPlansReport() {
        var plans = revenueAnalyticsService.getPlansReport();
        assertThat(plans).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Revenue dashboard
    // -------------------------------------------------------------------------

    @Autowired private RevenueDashboardService revenueDashboardService;

    @Test
    public void revenue_dashboard_getSummary() {
        revenueDashboardService.refreshMaterializedViews();
        RevenueSummaryResponse summary = revenueDashboardService.getSummary();
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalRevenue()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void revenue_dashboard_getTrends() {
        revenueDashboardService.refreshMaterializedViews();
        var trends = revenueDashboardService.getTrends();
        assertThat(trends).isNotNull();
    }

    @Test
    public void revenue_dashboard_getGrowth() {
        revenueDashboardService.refreshMaterializedViews();
        var growth = revenueDashboardService.getGrowth();
        assertThat(growth).isNotNull();
    }

    @Test
    public void revenue_dashboard_getForecast() {
        RevenueForecastResponse forecast = revenueDashboardService.getForecast(6);
        assertThat(forecast).isNotNull();
        assertThat(forecast.getHorizonMonths()).isEqualTo(6);
        assertThat(forecast.getForecastConfidenceScore()).isBetween(0.0, 100.0);
    }

    // -------------------------------------------------------------------------
    // Revenue exports (CSV / Excel / PDF)
    // -------------------------------------------------------------------------

    @Autowired private RevenueReportExportService revenueExportService;

    @Test
    public void revenue_export_paymentsCsv() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("PAYMENTS");
        request.setFormat("CSV");

        byte[] bytes = revenueExportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void revenue_export_invoicesExcel() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("INVOICES");
        request.setFormat("EXCEL");

        byte[] bytes = revenueExportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void revenue_export_summaryPdf() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("SUMMARY");
        request.setFormat("PDF");

        byte[] bytes = revenueExportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    // -------------------------------------------------------------------------
    // Revenue invoice repository
    // -------------------------------------------------------------------------

    @Autowired private RevenueInvoiceRepository invoiceRepository;
    @Autowired private InvoiceSpecification invoiceSpecification;

    @Test
    public void revenue_invoice_repository_queryWithSpecification() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setInvoiceStatus("PAID");

        var spec = invoiceSpecification.withFilters(filters);
        var page = invoiceRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Revenue payment repository
    // -------------------------------------------------------------------------

    @Autowired private RevenuePaymentRepository paymentRepository;
    @Autowired private PaymentSpecification paymentSpecification;

    @Test
    public void revenue_payment_repository_queryWithSpecification() {
        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setCurrency("INR");
        filters.setPaymentStatus("SUCCESS");

        var spec = paymentSpecification.withFilters(filters);
        var page = paymentRepository.findAll(spec, PageRequest.of(0, 10));
        assertThat(page).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Revenue dashboard repository
    // -------------------------------------------------------------------------

    @Autowired private RevenueDashboardRepository revenueDashboardRepository;

    @Test
    public void revenue_dashboard_repository_overallTotals() {
        revenueDashboardRepository.refreshDailyView();
        var totals = revenueDashboardRepository.getOverallDashboardTotals();
        assertThat(totals).isNotNull();
        assertThat(totals.isEmpty()).isFalse();
    }

    @Test
    public void revenue_dashboard_repository_calculateActiveMrr() {
        BigDecimal mrr = revenueDashboardRepository.calculateActiveMrr();
        assertThat(mrr).isNotNull();
        assertThat(mrr).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void revenue_dashboard_repository_getMonthlyRevenueTrends() {
        revenueDashboardRepository.refreshMonthlyView();
        var trends = revenueDashboardRepository.getMonthlyRevenueTrends();
        assertThat(trends).isNotNull();
    }

    @Test
    public void revenue_dashboard_repository_getPlanRevenueDistribution() {
        var distribution = revenueDashboardRepository.getPlanRevenueDistribution();
        assertThat(distribution).isNotNull();
    }

    @Test
    public void revenue_dashboard_repository_getRefundReasonsDistribution() {
        var refunds = revenueDashboardRepository.getRefundReasonsDistribution();
        assertThat(refunds).isNotNull();
    }

    @Test
    public void revenue_dashboard_repository_getTopCustomers() {
        var topCustomers = revenueDashboardRepository.getTopCustomers();
        assertThat(topCustomers).isNotNull();
    }
}
