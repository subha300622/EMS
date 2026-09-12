package com.example.ems.reports.organization;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.common.ReportExportStatus;
import com.example.ems.reports.common.ReportType;
import com.example.ems.reports.config.StorageService;
import com.example.ems.reports.exception.ReportNotFoundException;
import com.example.ems.reports.export.ExporterRegistry;
import com.example.ems.reports.export.ReportExportHistory;
import com.example.ems.reports.export.ReportExportHistoryRepository;
import com.example.ems.reports.export.ReportExporter;
import com.example.ems.reports.organization.dto.ExportHistoryResponse;
import com.example.ems.reports.organization.mapper.ExportHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
public class OrganizationReportExportService {

    @Autowired
    private ReportExportHistoryRepository exportHistoryRepository;

    @Autowired
    private OrganizationAnalyticsService analyticsService;

    @Autowired
    private ExporterRegistry exporterRegistry;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ExportHistoryMapper mapper;

    @Transactional(readOnly = true)
    public Page<ExportHistoryResponse> getExportHistory(String createdBy, Pageable pageable) {
        return exportHistoryRepository.findByCreatedBy(createdBy, pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReportExportHistory getExportById(Long id) {
        return exportHistoryRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Export history not found with ID: " + id));
    }

    @Transactional
    public ReportExportHistory initExport(ExportFormat format, String createdBy) {
        ReportExportHistory history = new ReportExportHistory();
        history.setReportName("Organization_Report_" + UUID.randomUUID().toString().substring(0, 8));
        history.setReportType(ReportType.ORGANIZATION);
        history.setExportFormat(format);
        history.setCreatedBy(createdBy);
        history.setStatus(ReportExportStatus.PENDING);
        return exportHistoryRepository.save(history);
    }

    @Async("reportExecutor")
    public void processExportAsync(Long exportId, String search, String status, String plan) {
        ReportExportHistory history = exportHistoryRepository.findById(exportId).orElse(null);
        if (history == null) return;

        try {
            history.setStatus(ReportExportStatus.PROCESSING);
            exportHistoryRepository.save(history);

            var items = analyticsService.getOrganizationList(search, status, plan, Pageable.unpaged()).getContent();

            List<String> headers = Arrays.asList(
                    "ID", "Code", "Name", "Email", "Status", "Plan", "Users", "Active Users", "Created Date"
            );
            List<List<String>> rows = new ArrayList<>();
            for (var item : items) {
                rows.add(Arrays.asList(
                        String.valueOf(item.getOrganizationId()),
                        item.getOrganizationCode() != null ? item.getOrganizationCode() : "",
                        item.getOrganizationName() != null ? item.getOrganizationName() : "",
                        item.getEmail() != null ? item.getEmail() : "",
                        item.getStatus() != null ? item.getStatus() : "",
                        item.getSubscriptionPlan() != null ? item.getSubscriptionPlan() : "",
                        String.valueOf(item.getOrganizationUserCount()),
                        String.valueOf(item.getActiveUsers()),
                        item.getCreatedDate() != null ? item.getCreatedDate() : ""
                ));
            }

            ReportExporter exporter = exporterRegistry.getExporter(history.getExportFormat());
            byte[] fileBytes = exporter.export("Organizations Report", headers, rows);

            String extension = history.getExportFormat() == ExportFormat.CSV ? ".csv" :
                    (history.getExportFormat() == ExportFormat.EXCEL ? ".xlsx" : ".pdf");
            String fileName = history.getReportName() + extension;
            storageService.store(fileBytes, fileName);

            history.setStatus(ReportExportStatus.COMPLETED);
            history.setDownloadUrl("/api/v1/platform/reports/organizations/export/download/" + exportId);
            exportHistoryRepository.save(history);

        } catch (Exception e) {
            e.printStackTrace();
            history.setStatus(ReportExportStatus.FAILED);
            exportHistoryRepository.save(history);
        }
    }

    @Transactional(readOnly = true)
    public InputStream getExportFile(Long id) {
        ReportExportHistory history = getExportById(id);
        if (history.getStatus() != ReportExportStatus.COMPLETED) {
            throw new IllegalStateException("Export file is not ready yet or generation failed.");
        }
        String extension = history.getExportFormat() == ExportFormat.CSV ? ".csv" :
                (history.getExportFormat() == ExportFormat.EXCEL ? ".xlsx" : ".pdf");
        String fileName = history.getReportName() + extension;
        return storageService.retrieve(fileName);
    }
}
