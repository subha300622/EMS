package com.example.ems.reports.service;

import com.example.ems.reports.revenue.dto.RevenueExportRequest;
import com.example.ems.reports.revenue.service.RevenueReportExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RevenueReportExportServiceTest {

    @Autowired
    private RevenueReportExportService exportService;

    @Test
    public void testExportPaymentsCsv() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("PAYMENTS");
        request.setFormat("CSV");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void testExportInvoicesExcel() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("INVOICES");
        request.setFormat("EXCEL");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void testExportSummaryPdf() {
        RevenueExportRequest request = new RevenueExportRequest();
        request.setType("SUMMARY");
        request.setFormat("PDF");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }
}
