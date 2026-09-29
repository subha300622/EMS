package com.example.ems.reports.organization;

import com.example.ems.reports.common.ExportFormat;
import com.example.ems.reports.export.ReportExportHistory;
import com.example.ems.reports.organization.dto.ExportHistoryResponse;
import com.example.ems.reports.organization.dto.OrganizationReportDetail;
import com.example.ems.reports.organization.dto.OrganizationReportListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class ReportFacade {

    @Autowired
    private OrganizationAnalyticsService analyticsService;

    @Autowired
    private OrganizationReportExportService exportService;

    public Page<OrganizationReportListItem> getOrganizationList(
            String search, String status, String plan, Pageable pageable) {
        return analyticsService.getOrganizationList(search, status, plan, pageable);
    }

    public List<OrganizationReportListItem> getTopOrganizations(String sortBy, int limit) {
        return analyticsService.getTopOrganizations(sortBy, limit);
    }

    public List<OrganizationReportListItem> getInactiveOrganizations(int days) {
        return analyticsService.getInactiveOrganizations(days);
    }

    public List<OrganizationReportListItem> getRecentlyRegistered(int days) {
        return analyticsService.getRecentlyRegistered(days);
    }

    public List<OrganizationReportListItem> getExpiringOrganizations(int days) {
        return analyticsService.getExpiringOrganizations(days);
    }

    public OrganizationReportDetail getOrganizationDetails(Long organizationId) {
        return analyticsService.getOrganizationDetails(organizationId);
    }

    public ReportExportHistory exportReport(ExportFormat format, String search, String status, String plan, String createdBy) {
        ReportExportHistory history = exportService.initExport(format, createdBy);
        exportService.processExportAsync(history.getId(), search, status, plan);
        return history;
    }

    public Page<ExportHistoryResponse> getExportHistory(String createdBy, Pageable pageable) {
        return exportService.getExportHistory(createdBy, pageable);
    }

    public InputStream getExportFile(Long id) {
        return exportService.getExportFile(id);
    }

    public ReportExportHistory getExportById(Long id) {
        return exportService.getExportById(id);
    }
}
