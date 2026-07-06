package com.example.ems.reports.organization;

import com.example.ems.reports.exception.ReportNotFoundException;
import com.example.ems.reports.organization.dto.OrganizationReportDetail;
import com.example.ems.reports.organization.dto.OrganizationReportListItem;
import com.example.ems.reports.organization.mapper.OrganizationReportMapper;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Subscription;
import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrganizationAnalyticsService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private OrganizationReportMapper mapper;

    public Page<OrganizationReportListItem> getOrganizationList(
            String search, String status, String plan, Pageable pageable) {

        List<Organization> orgs = organizationRepository.findAll().stream()
                .filter(o -> !o.isDeleted())
                .collect(Collectors.toList());

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.trim().toLowerCase();
            orgs = orgs.stream()
                    .filter(o -> o.getName().toLowerCase().contains(lowerSearch) ||
                            (o.getOrganizationCode() != null && o.getOrganizationCode().toLowerCase().contains(lowerSearch)) ||
                            (o.getEmail() != null && o.getEmail().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            String lowerStatus = status.trim().toLowerCase();
            orgs = orgs.stream()
                    .filter(o -> {
                        Subscription sub = o.getActiveSubscription();
                        String subStatus = sub != null ? sub.getStatus().name().toLowerCase() : "n/a";
                        return subStatus.equals(lowerStatus);
                    })
                    .collect(Collectors.toList());
        }

        if (plan != null && !plan.trim().isEmpty()) {
            String lowerPlan = plan.trim().toLowerCase();
            orgs = orgs.stream()
                    .filter(o -> {
                        Subscription sub = o.getActiveSubscription();
                        String subPlan = sub != null ? sub.getPlanCode().toLowerCase() : "n/a";
                        return subPlan.contains(lowerPlan);
                    })
                    .collect(Collectors.toList());
        }

        // Map to List Item
        List<OrganizationReportListItem> items = new ArrayList<>();
        for (Organization org : orgs) {
            long totalUsers = userRepository.countByOrganizationId(org.getId());
            long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();
            items.add(mapper.toListItem(org, totalUsers, activeUsers));
        }

        // Apply sorting manually in Java if required (Pageable contains sort)
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                String prop = order.getProperty();
                boolean asc = order.isAscending();
                items.sort((a, b) -> {
                    int cmp;
                    if ("name".equalsIgnoreCase(prop) || "organizationName".equalsIgnoreCase(prop)) {
                        cmp = a.getOrganizationName().compareToIgnoreCase(b.getOrganizationName());
                    } else if ("userCount".equalsIgnoreCase(prop) || "organizationUserCount".equalsIgnoreCase(prop)) {
                        cmp = Long.compare(a.getOrganizationUserCount(), b.getOrganizationUserCount());
                    } else if ("activeUsers".equalsIgnoreCase(prop)) {
                        cmp = Long.compare(a.getActiveUsers(), b.getActiveUsers());
                    } else if ("createdDate".equalsIgnoreCase(prop)) {
                        cmp = a.getCreatedDate().compareTo(b.getCreatedDate());
                    } else {
                        cmp = Long.compare(a.getOrganizationId(), b.getOrganizationId());
                    }
                    return asc ? cmp : -cmp;
                });
            });
        }

        if (pageable.isUnpaged()) {
            return new PageImpl<>(items, pageable, items.size());
        }

        int start = (int) pageable.getOffset();
        if (start >= items.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, items.size());
        }
        int end = Math.min((start + pageable.getPageSize()), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    public List<OrganizationReportListItem> getTopOrganizations(String sortBy, int limit) {
        List<Organization> orgs = organizationRepository.findAll().stream()
                .filter(o -> !o.isDeleted())
                .collect(Collectors.toList());

        List<OrganizationReportListItem> items = new ArrayList<>();
        for (Organization org : orgs) {
            long totalUsers = userRepository.countByOrganizationId(org.getId());
            long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();
            items.add(mapper.toListItem(org, totalUsers, activeUsers));
        }

        // Sort dynamically based on parameter
        items.sort((a, b) -> {
            int cmp;
            if ("employees".equalsIgnoreCase(sortBy) || "userCount".equalsIgnoreCase(sortBy)) {
                cmp = Long.compare(a.getOrganizationUserCount(), b.getOrganizationUserCount());
            } else if ("activeUsers".equalsIgnoreCase(sortBy)) {
                cmp = Long.compare(a.getActiveUsers(), b.getActiveUsers());
            } else if ("revenue".equalsIgnoreCase(sortBy)) {
                cmp = Double.compare(a.getOrganizationUserCount() * 35.0, b.getOrganizationUserCount() * 35.0);
            } else if ("storage".equalsIgnoreCase(sortBy)) {
                cmp = Double.compare(a.getOrganizationUserCount() * 0.15, b.getOrganizationUserCount() * 0.15);
            } else {
                cmp = Long.compare(a.getOrganizationId(), b.getOrganizationId());
            }
            return -cmp; // Descending order
        });

        return items.stream().limit(limit).collect(Collectors.toList());
    }

    public List<OrganizationReportListItem> getInactiveOrganizations(int days) {
        List<Organization> orgs = organizationRepository.findAll().stream()
                .filter(o -> !o.isDeleted())
                .collect(Collectors.toList());

        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<OrganizationReportListItem> inactiveList = new ArrayList<>();

        for (Organization org : orgs) {
            long totalUsers = userRepository.countByOrganizationId(org.getId());
            long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();

            List<String> emails = userRepository.findByOrganizationId(org.getId()).stream()
                    .map(com.example.ems.auth.entity.User::getWorkEmail)
                    .collect(Collectors.toList());

            long auditCount = 0;
            if (!emails.isEmpty()) {
                auditCount = auditLogRepository.countByUserEmailInAndCreatedAtAfter(emails, threshold);
            }

            if (auditCount == 0) {
                inactiveList.add(mapper.toListItem(org, totalUsers, activeUsers));
            }
        }

        return inactiveList;
    }

    public List<OrganizationReportListItem> getRecentlyRegistered(int days) {
        List<Organization> orgs = organizationRepository.findAll().stream()
                .filter(o -> !o.isDeleted())
                .collect(Collectors.toList());

        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        orgs = orgs.stream()
                .filter(o -> o.getCreatedAt().isAfter(threshold))
                .collect(Collectors.toList());

        List<OrganizationReportListItem> items = new ArrayList<>();
        for (Organization org : orgs) {
            long totalUsers = userRepository.countByOrganizationId(org.getId());
            long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();
            items.add(mapper.toListItem(org, totalUsers, activeUsers));
        }

        // Sort descending by created date (newest first)
        items.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
        return items;
    }

    public List<OrganizationReportListItem> getExpiringOrganizations(int days) {
        List<Organization> orgs = organizationRepository.findAll().stream()
                .filter(o -> !o.isDeleted())
                .collect(Collectors.toList());

        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusDays(days);

        orgs = orgs.stream()
                .filter(o -> {
                    Subscription sub = o.getActiveSubscription();
                    if (sub != null && sub.getStatus() == SubscriptionStatus.ACTIVE && sub.getExpiryDate() != null) {
                        return !sub.getExpiryDate().isBefore(now) && !sub.getExpiryDate().isAfter(threshold);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<OrganizationReportListItem> items = new ArrayList<>();
        for (Organization org : orgs) {
            long totalUsers = userRepository.countByOrganizationId(org.getId());
            long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();
            items.add(mapper.toListItem(org, totalUsers, activeUsers));
        }

        return items;
    }

    public OrganizationReportDetail getOrganizationDetails(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new ReportNotFoundException("Organization not found or has been deleted."));

        long userCount = userRepository.countByOrganizationId(org.getId());
        long activeUsers = userRepository.findByOrganizationId(org.getId()).stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .count();

        long departmentCount = organizationRepository.countDepartments(org.getId());
        long roleCount = userRepository.findByOrganizationId(org.getId()).stream()
                .map(com.example.ems.auth.entity.User::getRole)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double revenue = userCount * 35.00;
        double storageUsedGB = userCount * 0.15;

        OrganizationReportDetail detail = mapper.toDetail(org, userCount, activeUsers, departmentCount, roleCount, storageUsedGB, revenue);

        // Fetch logs size for user emails
        List<String> emails = userRepository.findByOrganizationId(org.getId()).stream()
                .map(com.example.ems.auth.entity.User::getWorkEmail)
                .collect(Collectors.toList());

        long auditCount = 0;
        if (!emails.isEmpty()) {
            auditCount = auditLogRepository.findAll().stream()
                    .filter(log -> emails.contains(log.getUserEmail()))
                    .count();
        }

        detail.getAuditSummary().put("totalLogs", auditCount);
        detail.getAuditSummary().put("lastActive", Instant.now().toString());

        return detail;
    }
}
