package com.example.ems.reports.service;

import com.example.ems.reports.subscription.dto.SubscriptionExportRequest;
import com.example.ems.reports.subscription.service.SubscriptionReportExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SubscriptionReportExportServiceTest {

    @Autowired
    private SubscriptionReportExportService exportService;

    @Test
    public void testExportCsv() {
        SubscriptionExportRequest request = new SubscriptionExportRequest();
        request.setFormat("CSV");
        request.setSortBy("organizationId");
        request.setDirection("asc");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void testExportExcel() {
        SubscriptionExportRequest request = new SubscriptionExportRequest();
        request.setFormat("EXCEL");
        request.setSortBy("organizationId");
        request.setDirection("asc");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }

    @Test
    public void testExportPdf() {
        SubscriptionExportRequest request = new SubscriptionExportRequest();
        request.setFormat("PDF");
        request.setSortBy("organizationId");
        request.setDirection("asc");

        byte[] bytes = exportService.exportReport(request);
        assertThat(bytes).isNotEmpty();
    }
}
