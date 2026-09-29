package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.audit.repository.AuditLogSpecification;
import com.example.ems.auth.entity.User;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditLogExportServiceImpl implements AuditLogExportService {

    private static final int MAX_EXPORT_RECORDS = 5000;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public byte[] exportAuditLogsCsv(AuditLogFilterRequest request, User currentUser) {
        Long enforcedCompanyId = null;
        Long departmentScope = null;
        Collection<String> allowedModules = null;

        if (currentUser != null && currentUser.getRole() != null) {
            String roleName = currentUser.getRole().getName();
            boolean isSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(roleName) || "PLATFORM_ADMIN".equalsIgnoreCase(roleName);

            if (!isSuperAdmin) {
                enforcedCompanyId = currentUser.getOrganizationId() != null
                        ? currentUser.getOrganizationId()
                        : TenantContext.getOrganizationId();
            }

            if ("DEPARTMENT_MANAGER".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName)) {
                departmentScope = currentUser.getDepartmentId();
            }

            if ("FINANCE".equalsIgnoreCase(roleName)) {
                allowedModules = List.of("PAYROLL", "EXPENSE", "FINANCE", "INCREMENT", "SETTLEMENT", "Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment", "F&F Settlement");
            } else if ("HR".equalsIgnoreCase(roleName) || "HR_MANAGER".equalsIgnoreCase(roleName)) {
                allowedModules = List.of("EMPLOYEE", "LEAVE", "ATTENDANCE", "RECRUITMENT", "ONBOARDING", "OFFBOARDING", "Employee", "Recruitment", "Leave", "Onboarding", "Offboarding");
            }
        }

        Specification<AuditLog> spec = AuditLogSpecification.filter(request, enforcedCompanyId, departmentScope, allowedModules);
        List<AuditLog> logs = auditLogRepository.findAll(spec, PageRequest.of(0, MAX_EXPORT_RECORDS, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        return generateCsv(logs);
    }

    @Override
    public byte[] exportLogsToCsv(Collection<String> allowedModules) {
        List<AuditLog> logs;
        if (allowedModules != null && !allowedModules.isEmpty()) {
            Specification<AuditLog> spec = AuditLogSpecification.filter(null, null, null, null, null, null, null, null, allowedModules);
            logs = auditLogRepository.findAll(spec);
        } else {
            logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return generateCsv(logs);
    }

    private byte[] generateCsv(List<AuditLog> logs) {
        StringBuilder csv = new StringBuilder(
                "ID,Timestamp,Company ID,User ID,User Email,User Name,Department ID,Module,Action,Entity Type,Record ID,Permission,HTTP Method,API Path,IP Address,Device,Browser,Status,Failure Reason,Request ID,Details\n");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",")
                    .append(log.getCreatedAt()).append(",")
                    .append(log.getCompanyId() != null ? log.getCompanyId() : "").append(",")
                    .append(escapeCsvField(log.getUserId())).append(",")
                    .append(escapeCsvField(log.getUserEmail())).append(",")
                    .append(escapeCsvField(log.getUserName())).append(",")
                    .append(log.getDepartmentId() != null ? log.getDepartmentId() : "").append(",")
                    .append(escapeCsvField(log.getModule())).append(",")
                    .append(escapeCsvField(log.getAction())).append(",")
                    .append(escapeCsvField(log.getEntityType())).append(",")
                    .append(escapeCsvField(log.getRecordId())).append(",")
                    .append(escapeCsvField(log.getPermission())).append(",")
                    .append(escapeCsvField(log.getHttpMethod())).append(",")
                    .append(escapeCsvField(log.getApiPath())).append(",")
                    .append(escapeCsvField(log.getIpAddress())).append(",")
                    .append(escapeCsvField(log.getDevice())).append(",")
                    .append(escapeCsvField(log.getBrowser())).append(",")
                    .append(escapeCsvField(log.getStatus())).append(",")
                    .append(escapeCsvField(log.getFailureReason())).append(",")
                    .append(escapeCsvField(log.getRequestId())).append(",")
                    .append(escapeCsvField(log.getDetails())).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        String clean = field.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"") || clean.contains("\r")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }
}
