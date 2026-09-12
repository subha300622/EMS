package com.example.ems.reports.subscription.facade;

import com.example.ems.reports.subscription.dto.*;
import com.example.ems.reports.subscription.service.SubscriptionAnalyticsService;
import com.example.ems.reports.subscription.service.SubscriptionReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionReportFacade {

    @Autowired
    @Qualifier("reportsSubscriptionAnalyticsService")
    private SubscriptionAnalyticsService analyticsService;

    @Autowired
    private SubscriptionReportExportService exportService;

    public Page<OrgSubscriptionListItem> getSubscriptionList(SubscriptionReportFilterRequest filter) {
        return analyticsService.getSubscriptionList(filter);
    }

    public Page<ExpiringSubscriptionEntry> getExpiringSubscriptions(int days, Pageable pageable) {
        return analyticsService.getExpiringSubscriptions(days, pageable);
    }

    public Page<TrialOrganizationEntry> getTrialOrganizations(Pageable pageable) {
        return analyticsService.getTrialOrganizations(pageable);
    }

    public SubscriptionDetailResponse getSubscriptionDetail(Long organizationId) {
        return analyticsService.getSubscriptionDetail(organizationId);
    }

    public byte[] exportReport(SubscriptionExportRequest request) {
        return exportService.exportReport(request);
    }
}
