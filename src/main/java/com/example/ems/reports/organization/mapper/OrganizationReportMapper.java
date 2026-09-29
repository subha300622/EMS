package com.example.ems.reports.organization.mapper;

import com.example.ems.reports.organization.dto.OrganizationReportDetail;
import com.example.ems.reports.organization.dto.OrganizationReportListItem;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.Subscription;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrganizationReportMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public OrganizationReportListItem toListItem(Organization org, long userCount, long activeUsers) {
        Subscription sub = org.getActiveSubscription();
        String plan = sub != null ? sub.getPlanCode() : "N/A";
        String status = sub != null ? sub.getStatus().name() : "N/A";
        String createdDate = org.getCreatedAt() != null ? DATE_FORMATTER.format(org.getCreatedAt()) : "N/A";

        return new OrganizationReportListItem(
                org.getId(),
                org.getOrganizationCode(),
                org.getName(),
                org.getEmail(),
                status,
                plan,
                userCount,
                activeUsers,
                createdDate
        );
    }

    public OrganizationReportDetail toDetail(
            Organization org, long userCount, long activeUsers,
            long departmentCount, long roleCount, double storageUsedGB, double revenue) {
        Subscription sub = org.getActiveSubscription();
        String plan = sub != null ? sub.getPlanCode() : "N/A";
        String status = sub != null ? sub.getStatus().name() : "N/A";
        String startDate = (sub != null && sub.getStartDate() != null) ? sub.getStartDate().toString() : "N/A";
        String expiryDate = (sub != null && sub.getExpiryDate() != null) ? sub.getExpiryDate().toString() : "N/A";
        String createdDate = org.getCreatedAt() != null ? DATE_FORMATTER.format(org.getCreatedAt()) : "N/A";

        OrganizationReportDetail detail = new OrganizationReportDetail();
        detail.setOrganizationId(org.getId());
        detail.setOrganizationCode(org.getOrganizationCode());
        detail.setOrganizationName(org.getName());
        detail.setEmail(org.getEmail());
        detail.setPhone(org.getPhone());
        detail.setWebsite(org.getWebsite());
        detail.setCreatedDate(createdDate);

        detail.setSubscriptionPlan(plan);
        detail.setStatus(status);
        detail.setSubscriptionStartDate(startDate);
        detail.setSubscriptionExpiryDate(expiryDate);

        detail.setOrganizationUserCount(userCount);
        detail.setActiveUsers(activeUsers);
        detail.setDepartmentCount(departmentCount);
        detail.setRoleCount(roleCount);
        detail.setStorageUsedGB(storageUsedGB);
        detail.setRevenue(revenue);

        List<String> modules = new ArrayList<>();
        if (org.getSettings() != null && org.getSettings().getConfig() != null) {
            for (Map.Entry<String, Object> entry : org.getSettings().getConfig().entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    modules.add(entry.getKey());
                }
            }
        }
        detail.setModulesEnabled(modules);

        Map<String, Object> auditSummary = new HashMap<>();
        auditSummary.put("totalLogs", 0L);
        detail.setAuditSummary(auditSummary);

        return detail;
    }
}
