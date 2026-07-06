package com.example.ems.reports.revenue.facade;

import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.service.RevenueAnalyticsService;
import com.example.ems.reports.revenue.service.RevenueReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RevenueReportFacade {

    @Autowired
    private RevenueAnalyticsService analyticsService;

    @Autowired
    private RevenueReportExportService exportService;

    public Page<RevenuePaymentResponse> getPaymentsReport(RevenueFilterRequest filters) {
        return analyticsService.getPaymentsReport(filters);
    }

    public Page<RevenueInvoiceResponse> getInvoicesReport(RevenueFilterRequest filters) {
        return analyticsService.getInvoicesReport(filters);
    }

    public Page<RevenueRefundResponse> getRefundsReport(RevenueFilterRequest filters) {
        return analyticsService.getRefundsReport(filters);
    }

    public List<RevenuePlanDistributionResponse> getPlansReport() {
        return analyticsService.getPlansReport();
    }

    public byte[] exportReport(RevenueExportRequest request) {
        return exportService.exportReport(request);
    }
}
