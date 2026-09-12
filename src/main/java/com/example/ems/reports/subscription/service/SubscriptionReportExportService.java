package com.example.ems.reports.subscription.service;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.export.ExporterRegistry;
import com.example.ems.reports.export.ReportExporter;
import com.example.ems.reports.subscription.dto.SubscriptionExportRequest;
import com.example.ems.reports.subscription.export.SubscriptionExportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionReportExportService {

    @Autowired
    @Qualifier("reportsSubscriptionAnalyticsService")
    private SubscriptionAnalyticsService analyticsService;

    @Autowired
    private SubscriptionExportMapper exportMapper;

    @Autowired
    private ExporterRegistry exporterRegistry;

    public byte[] exportReport(SubscriptionExportRequest request) {
        var items = analyticsService.getSubscriptionListUnpaged(request);

        List<String> headers = exportMapper.getHeaders();
        List<List<String>> rows = exportMapper.mapToRows(items);

        ExportFormat format = ExportFormat.valueOf(request.getFormat().toUpperCase());
        ReportExporter exporter = exporterRegistry.getExporter(format);

        return exporter.export("Subscriptions Report", headers, rows);
    }
}
