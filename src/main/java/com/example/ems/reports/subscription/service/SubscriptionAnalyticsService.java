package com.example.ems.reports.subscription.service;

import com.example.ems.organization.entity.Subscription;
import com.example.ems.reports.exception.ReportNotFoundException;
import com.example.ems.reports.subscription.dto.*;
import com.example.ems.reports.subscription.mapper.SubscriptionReportMapper;
import com.example.ems.reports.subscription.repository.SubscriptionReportRepository;
import com.example.ems.reports.subscription.specification.SubscriptionReportSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("reportsSubscriptionAnalyticsService")
@Transactional(readOnly = true)
public class SubscriptionAnalyticsService {

    @Autowired
    private SubscriptionReportRepository reportRepository;

    @Autowired
    private SubscriptionReportMapper mapper;

    public Page<OrgSubscriptionListItem> getSubscriptionList(SubscriptionReportFilterRequest filter) {
        var spec = SubscriptionReportSpecification.withFilters(
                filter.getSearch(), 
                filter.getStatus(), 
                filter.getPlan(), 
                filter.getBillingCycle(), 
                filter.getFromDate(), 
                filter.getToDate()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filter.getDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Subscription> page = reportRepository.findAll(spec, pageable);
        return page.map(mapper::toListItem);
    }

    public List<OrgSubscriptionListItem> getSubscriptionListUnpaged(SubscriptionExportRequest filter) {
        var spec = SubscriptionReportSpecification.withFilters(
                filter.getSearch(), 
                filter.getStatus(), 
                filter.getPlan(), 
                filter.getBillingCycle(), 
                filter.getFromDate(), 
                filter.getToDate()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filter.getDirection()), filter.getSortBy());
        List<Subscription> list = reportRepository.findAll(spec, sort);
        return list.stream().map(mapper::toListItem).collect(Collectors.toList());
    }

    public Page<ExpiringSubscriptionEntry> getExpiringSubscriptions(int days, Pageable pageable) {
        LocalDate now = LocalDate.now();
        LocalDate target = now.plusDays(days);
        Page<Subscription> page = reportRepository.findExpiringSubscriptions(now, target, pageable);
        return page.map(mapper::toExpiringEntry);
    }

    public Page<TrialOrganizationEntry> getTrialOrganizations(Pageable pageable) {
        Page<Subscription> page = reportRepository.findTrialSubscriptions(pageable);
        return page.map(mapper::toTrialEntry);
    }

    public SubscriptionDetailResponse getSubscriptionDetail(Long organizationId) {
        Subscription subscription = reportRepository.findByOrganizationIdWithOrganization(organizationId)
                .orElseThrow(() -> new ReportNotFoundException("No subscription found for organization with ID: " + organizationId));
        return mapper.toDetailResponse(subscription);
    }
}
