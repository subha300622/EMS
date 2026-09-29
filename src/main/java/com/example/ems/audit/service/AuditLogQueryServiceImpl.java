package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditDashboardStatsDto;
import com.example.ems.audit.dto.AuditLogDetailResponse;
import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.dto.AuditLogResponse;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.audit.repository.AuditLogSpecification;
import com.example.ems.auth.entity.User;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired(required = false)
    private LeaveRepository leaveRepository;

    @Autowired(required = false)
    private ExpenseRepository expenseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<AuditLogResponse> getLogs(AuditLogFilterRequest request, User currentUser, Pageable pageable) {
        Long enforcedCompanyId = resolveEnforcedCompanyId(currentUser);
        Long departmentScope = resolveDepartmentScope(currentUser);
        Collection<String> allowedModules = resolveAllowedModules(currentUser);

        Specification<AuditLog> spec = AuditLogSpecification.filter(request, enforcedCompanyId, departmentScope, allowedModules);
        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public AuditLogDetailResponse getLogById(Long id, User currentUser) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Audit log not found with ID: " + id));

        validateAccess(log, currentUser);
        return toDetailResponse(log);
    }

    @Override
    public List<AuditLogResponse> getEmployeeAuditHistory(String employeeId, User currentUser) {
        Long companyId = resolveEnforcedCompanyId(currentUser);
        List<AuditLog> logs;
        if (companyId != null) {
            logs = auditLogRepository.findByCompanyIdAndRecordIdOrEntityIdOrderByCreatedAtDesc(companyId, employeeId);
        } else {
            logs = auditLogRepository.findByRecordIdOrEntityIdOrderByCreatedAtDesc(employeeId);
        }

        Long deptScope = resolveDepartmentScope(currentUser);
        if (deptScope != null) {
            logs = logs.stream()
                    .filter(l -> l.getDepartmentId() == null || deptScope.equals(l.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        return logs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Page<AuditLogResponse> getMyActivity(User currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new AccessDeniedException("User must be authenticated to view activity.");
        }

        Long companyId = resolveEnforcedCompanyId(currentUser);
        String userId = currentUser.getUserId() != null ? currentUser.getUserId() : currentUser.getWorkEmail();

        Page<AuditLog> logs;
        if (companyId != null) {
            logs = auditLogRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyId, userId, pageable);
        } else {
            logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return logs.map(this::toResponse);
    }

    @Override
    public AuditDashboardStatsDto getDashboardStats(User currentUser) {
        Collection<String> allowed = resolveAllowedModules(currentUser);
        return getDashboardStats(allowed);
    }

    @Override
    public AuditDashboardStatsDto getDashboardStats(Collection<String> allowedModules) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7).toLocalDate().atStartOfDay();

        List<String> financeModules = List.of("Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment",
                "F&F Settlement", "PAYROLL", "EXPENSE", "FINANCE");
        List<String> payrollModules = List.of("Payroll", "Payroll Settings", "PAYROLL");

        Collection<String> financeScoped = allowedModules != null
                ? allowedModules.stream().filter(financeModules::contains).collect(Collectors.toList())
                : financeModules;

        long financeActionsToday = auditLogRepository.countByEntityTypeInAndCreatedAtAfter(financeScoped, startOfDay);
        long flaggedCount = allowedModules != null
                ? auditLogRepository.countByFlaggedTrueAndEntityTypeIn(allowedModules)
                : auditLogRepository.countByFlaggedTrue();

        Collection<String> payrollScoped = allowedModules != null
                ? allowedModules.stream().filter(payrollModules::contains).collect(Collectors.toList())
                : payrollModules;
        long payrollEventsThisWeek = auditLogRepository.countByEntityTypeInAndCreatedAtAfter(payrollScoped,
                startOfWeek);

        long pendingLeaves = leaveRepository != null ? leaveRepository.findByStatus("PENDING").size() : 0;
        long pendingExpenses = expenseRepository != null ? expenseRepository.findByStatus(ExpenseStatus.PENDING).size() : 0;
        long pendingApprovals = pendingLeaves + pendingExpenses;

        return new AuditDashboardStatsDto(
                financeActionsToday,
                flaggedCount,
                payrollEventsThisWeek,
                pendingApprovals
        );
    }

    @Override
    public Page<AuditLog> getFilteredLogs(
            String search, String module, String action, String user,
            String date, LocalDateTime from, LocalDateTime to, Severity severity, Boolean flagged,
            Collection<String> allowedModules, Pageable pageable) {

        LocalDateTime startDateTime = from;
        LocalDateTime endDateTime = to;
        LocalDateTime now = LocalDateTime.now();

        if (startDateTime == null && endDateTime == null && date != null && !date.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(date)) {
            String d = date.trim().toUpperCase();
            if ("TODAY".equals(d)) {
                startDateTime = now.toLocalDate().atStartOfDay();
                endDateTime = now.toLocalDate().atTime(23, 59, 59);
            } else if ("YESTERDAY".equals(d)) {
                startDateTime = now.toLocalDate().minusDays(1).atStartOfDay();
                endDateTime = now.toLocalDate().minusDays(1).atTime(23, 59, 59);
            } else if ("WEEK".equals(d) || "THIS_WEEK".equals(d)) {
                startDateTime = now.minusDays(7).toLocalDate().atStartOfDay();
            } else if ("MONTH".equals(d) || "THIS_MONTH".equals(d)) {
                startDateTime = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            }
        }

        Specification<AuditLog> spec = AuditLogSpecification.filter(search, module,
                action, user, startDateTime, endDateTime, severity, flagged, allowedModules);

        return auditLogRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public AuditLog reviewLog(Long id, String reviewerUsername, String remarks) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found with ID: " + id));
        log.setFlagged(false);
        log.setReviewedBy(reviewerUsername);
        log.setReviewedAt(LocalDateTime.now());
        if (remarks != null && !remarks.trim().isEmpty()) {
            log.setDetails(log.getDetails() + " (Reviewed: " + remarks + ")");
        }
        return auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void dismissAllFlags(String reviewerUsername) {
        List<AuditLog> flaggedLogs = auditLogRepository.findAll().stream()
                .filter(AuditLog::getFlagged)
                .collect(Collectors.toList());
        for (AuditLog log : flaggedLogs) {
            log.setFlagged(false);
            log.setReviewedBy(reviewerUsername);
            log.setReviewedAt(LocalDateTime.now());
            auditLogRepository.save(log);
        }
    }

    @Override
    public Optional<AuditLog> findLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    @Override
    public List<AuditLog> getLogsByUser(String userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<AuditLog> getLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Override
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    // Helper methods for scoping and transformations
    private Long resolveEnforcedCompanyId(User user) {
        if (user == null || user.getRole() == null) {
            return TenantContext.getOrganizationId();
        }
        String role = user.getRole().getName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "PLATFORM_ADMIN".equalsIgnoreCase(role)) {
            return null; // platform admin can query across tenants
        }
        return user.getOrganizationId() != null ? user.getOrganizationId() : TenantContext.getOrganizationId();
    }

    private Long resolveDepartmentScope(User user) {
        if (user == null || user.getRole() == null) {
            return null;
        }
        String role = user.getRole().getName();
        if ("DEPARTMENT_MANAGER".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
            return user.getDepartmentId();
        }
        return null;
    }

    private Collection<String> resolveAllowedModules(User user) {
        if (user == null || user.getRole() == null) {
            return null;
        }
        String role = user.getRole().getName();
        if ("SUPER_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "COMPANY_ADMIN".equalsIgnoreCase(role) || "PLATFORM_ADMIN".equalsIgnoreCase(role)) {
            return null;
        }
        if ("FINANCE".equalsIgnoreCase(role)) {
            return List.of("PAYROLL", "EXPENSE", "FINANCE", "INCREMENT", "SETTLEMENT", "Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment", "F&F Settlement");
        }
        if ("HR".equalsIgnoreCase(role) || "HR_MANAGER".equalsIgnoreCase(role)) {
            return List.of("EMPLOYEE", "LEAVE", "ATTENDANCE", "RECRUITMENT", "ONBOARDING", "OFFBOARDING", "Employee", "Recruitment", "Leave", "Onboarding", "Offboarding");
        }
        return null;
    }

    private void validateAccess(AuditLog log, User currentUser) {
        if (currentUser == null) return;
        Long enforcedCompanyId = resolveEnforcedCompanyId(currentUser);
        if (enforcedCompanyId != null && log.getCompanyId() != null && !enforcedCompanyId.equals(log.getCompanyId())) {
            throw new AccessDeniedException("Access Denied: Log belongs to another organization.");
        }

        Long deptScope = resolveDepartmentScope(currentUser);
        if (deptScope != null && log.getDepartmentId() != null && !deptScope.equals(log.getDepartmentId())) {
            throw new AccessDeniedException("Access Denied: Log outside of user's department scope.");
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(log.getId());
        dto.setCompanyId(log.getCompanyId());
        dto.setUserId(log.getUserId());
        dto.setUserName(log.getUserName());
        dto.setUserEmail(log.getUserEmail());
        dto.setDepartmentId(log.getDepartmentId());
        dto.setModule(log.getModule());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setRecordId(log.getRecordId());
        dto.setIpAddress(log.getIpAddress());
        dto.setDevice(log.getDevice());
        dto.setBrowser(log.getBrowser());
        dto.setStatus(log.getStatus());
        dto.setFailureReason(log.getFailureReason());
        dto.setRequestId(log.getRequestId());
        dto.setPermission(log.getPermission());
        dto.setHttpMethod(log.getHttpMethod());
        dto.setApiPath(log.getApiPath());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    private AuditLogDetailResponse toDetailResponse(AuditLog log) {
        AuditLogDetailResponse dto = new AuditLogDetailResponse();
        dto.setId(log.getId());
        dto.setCompanyId(log.getCompanyId());
        dto.setUserId(log.getUserId());
        dto.setUserName(log.getUserName());
        dto.setUserEmail(log.getUserEmail());
        dto.setDepartmentId(log.getDepartmentId());
        dto.setModule(log.getModule());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setRecordId(log.getRecordId());
        dto.setIpAddress(log.getIpAddress());
        dto.setDevice(log.getDevice());
        dto.setBrowser(log.getBrowser());
        dto.setStatus(log.getStatus());
        dto.setFailureReason(log.getFailureReason());
        dto.setRequestId(log.getRequestId());
        dto.setPermission(log.getPermission());
        dto.setHttpMethod(log.getHttpMethod());
        dto.setApiPath(log.getApiPath());
        dto.setDetails(log.getDetails());
        dto.setSeverity(log.getSeverity() != null ? log.getSeverity().name() : null);
        dto.setFlagged(log.getFlagged());
        dto.setCreatedAt(log.getCreatedAt());

        // Parse oldValue and newValue to structured JSON or string
        dto.setOldValue(parseJsonSafely(log.getOldValue()));
        dto.setNewValue(parseJsonSafely(log.getNewValue()));

        return dto;
    }

    private Object parseJsonSafely(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return json;
        }
    }
}
