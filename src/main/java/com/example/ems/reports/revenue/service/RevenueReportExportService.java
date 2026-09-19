package com.example.ems.reports.revenue.service;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.export.ExporterRegistry;
import com.example.ems.reports.revenue.dto.RevenueExportRequest;
import com.example.ems.reports.revenue.exception.InvalidRevenueExportFormatException;
import com.example.ems.reports.revenue.export.RevenueExportStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RevenueReportExportService {

    @Autowired
    private ExporterRegistry exporterRegistry;

    @Autowired
    private RevenueAnalyticsService analyticsService;

    @Autowired
    private RevenueDashboardService dashboardService;

    @Autowired
    private Map<String, RevenueExportStrategy> strategyMap;

    public byte[] exportReport(RevenueExportRequest request) {
        String typeKey = request.getType().toUpperCase() + "_ExportStrategy";
        RevenueExportStrategy strategy = strategyMap.get(typeKey);
        if (strategy == null) {
            throw new InvalidRevenueExportFormatException("Unsupported export strategy type: " + request.getType());
        }

        List<?> dataList;
        switch (request.getType().toUpperCase()) {
            case "PAYMENTS":
                dataList = analyticsService.getPaymentsReport(request).getContent();
                break;
            case "INVOICES":
                dataList = analyticsService.getInvoicesReport(request).getContent();
                break;
            case "REFUNDS":
                dataList = analyticsService.getRefundsReport(request).getContent();
                break;
            case "PLAN_REVENUE":
                dataList = analyticsService.getPlansReport();
                break;
            case "FORECAST":
                int horizon = request.getSize() != 10 ? request.getSize() : 6; // Reuse page limits/size for horizon or default to 6
                dataList = dashboardService.getForecast(horizon).getDataPoints();
                break;
            case "SUMMARY":
                dataList = List.of(dashboardService.getSummary());
                break;
            default:
                throw new InvalidRevenueExportFormatException("Unknown export report type: " + request.getType());
        }

        String title = strategy.getTitle();
        List<String> headers = strategy.getHeaders();
        List<List<String>> rows = strategy.serializeRows(dataList);

        ExportFormat formatEnum;
        try {
            formatEnum = ExportFormat.valueOf(request.getFormat().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRevenueExportFormatException("Invalid export format: " + request.getFormat());
        }

        return exporterRegistry.getExporter(formatEnum).export(title, headers, rows);
    }
}
