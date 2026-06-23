package com.example.ems.audit.service;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.audit.repository.AuditLogSpecification;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.expense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public AuditLog logAction(String userId, String userEmail, String action, String entityType, String entityId,
            String ipAddress, String details) {
        return logAction(userId, userEmail, null, action, entityType, entityId, ipAddress, null, Severity.INFO,
                details);
    }

    public AuditLog logAction(String userId, String userEmail, String userName, String action, String entityType,
            String entityId, String ipAddress, String device, Severity severity, String details) {
        AuditLog log = new AuditLog(userId, userEmail, userName, action, entityType, entityId, ipAddress, device,
                severity, details);
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
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

        org.springframework.data.jpa.domain.Specification<AuditLog> spec = AuditLogSpecification.filter(search, module,
                action, user, startDateTime, endDateTime, severity, flagged, allowedModules);

        return auditLogRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(Collection<String> allowedModules) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7).toLocalDate().atStartOfDay();

        List<String> financeModules = List.of("Payroll", "Expenses", "Finance Reports", "Payroll Settings", "Increment",
                "F&F Settlement");
        List<String> payrollModules = List.of("Payroll", "Payroll Settings");

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
        long pendingExpenses = expenseRepository != null ? expenseRepository.findByStatus(com.example.ems.expense.entity.ExpenseStatus.PENDING).size() : 0;
        long pendingApprovals = pendingLeaves + pendingExpenses;

        Map<String, Object> stats = new HashMap<>();
        stats.put("financeActionsToday", financeActionsToday);
        stats.put("flaggedCount", flaggedCount);
        stats.put("payrollEventsThisWeek", payrollEventsThisWeek);
        stats.put("pendingApprovalsThisMonth", pendingApprovals);

        return stats;
    }

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

    @Transactional(readOnly = true)
    public Optional<AuditLog> getLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(String userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public byte[] exportLogsToCsv() {
        return exportLogsToCsv(null);
    }

    @Transactional(readOnly = true)
    public byte[] exportLogsToCsv(Collection<String> allowedModules) {
        List<AuditLog> logs;
        if (allowedModules != null && !allowedModules.isEmpty()) {
            org.springframework.data.jpa.domain.Specification<AuditLog> spec = AuditLogSpecification.filter(null, null,
                    null, null, null, null, null, null, allowedModules);
            logs = auditLogRepository.findAll(spec);
        } else {
            logs = getAllLogs();
        }

        StringBuilder csv = new StringBuilder(
                "ID,Timestamp,User ID,User Email,User Name,Action,Entity Type,Entity ID,IP Address,Device,Severity,Flagged,Flag Reason,Reviewed By,Reviewed At,Details\n");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",")
                    .append(log.getCreatedAt()).append(",")
                    .append(escapeCsvField(log.getUserId())).append(",")
                    .append(escapeCsvField(log.getUserEmail())).append(",")
                    .append(escapeCsvField(log.getUserName())).append(",")
                    .append(escapeCsvField(log.getAction())).append(",")
                    .append(escapeCsvField(log.getEntityType())).append(",")
                    .append(escapeCsvField(log.getEntityId())).append(",")
                    .append(escapeCsvField(log.getIpAddress())).append(",")
                    .append(escapeCsvField(log.getDevice())).append(",")
                    .append(log.getSeverity() != null ? log.getSeverity().name() : "INFO").append(",")
                    .append(log.getFlagged()).append(",")
                    .append(escapeCsvField(log.getFlagReason())).append(",")
                    .append(escapeCsvField(log.getReviewedBy())).append(",")
                    .append(log.getReviewedAt() != null ? log.getReviewedAt().toString() : "").append(",")
                    .append(escapeCsvField(log.getDetails())).append("\n");
        }
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsvField(String field) {
        if (field == null)
            return "";
        String clean = field.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }

    @Transactional
    public void seedAuditLogs() {
        if (auditLogRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        int currentYear = today.getYear();

        // Seed logs matching Vercel UI
        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "RUN", "Payroll", "284", "20.0.0.8",
                "Chrome/Windows", Severity.INFO,
                "Payroll: April 2026 processing initiated — 284 employees", today.atTime(11, 30), false, null);

        createSeedLog("EMP009", "rahul@company.com", "Rahul Verma", "APPROVE", "Expenses", "EXP-4321", "20.0.0.1",
                "Safari/Mac", Severity.INFO,
                "Expense #EXP-4321 approved — $45,000 — James Carter", today.atTime(10, 15), false, null);

        createSeedLog("UNKNOWN", "unknown@company.com", "Unknown", "EXPORT", "Finance Reports", "REP-QBUDGET",
                "212.85.12.99", "Chrome/Windows", Severity.CRITICAL,
                "Quarterly budget report exported — external IP detected", today.atTime(10, 12), true,
                "External IP detected during export");

        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "UPDATE", "Payroll Settings", "SETT-TAX",
                "20.0.0.8", "Chrome/Windows", Severity.INFO,
                "TAX slab updated for FY 2026-27 — new regime", today.atTime(9, 35), false, null);

        createSeedLog("EMP009", "rahul@company.com", "Rahul Verma", "APPROVE", "Increment", "INC-MEERA", "20.0.0.1",
                "Safari/Mac", Severity.INFO,
                "Increment approved for Meera Thomas — 12% raise", today.atTime(9, 0), false, null);

        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "PROCESS", "F&F Settlement", "SETT-PRIYA",
                "20.0.0.8", "Chrome/Windows", Severity.INFO,
                "F&F settlement processed — Priya Sharma — $1,51,000", yesterday.atTime(17, 30), false, null);

        createSeedLog("EMP009", "rahul@company.com", "Rahul Verma", "APPROVE", "Expenses", "EXP-BULK", "20.0.0.1",
                "Safari/Mac", Severity.WARNING,
                "Bulk expense approval — 12 travel claims — $2,34,000", yesterday.atTime(16, 15), true,
                "Unusually large bulk approval amount");

        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "REJECT", "Expenses", "EXP-4189", "20.0.0.8",
                "Chrome/Windows", Severity.INFO,
                "Expense #EXP-4189 rejected — $12,000 — out of policy", yesterday.atTime(15, 0), false, null);

        createSeedLog("EMP012", "suresh@company.com", "Suresh Iyer", "VIEW", "Finance Reports", "REP-SALBUDGET",
                "20.0.0.12", "Chrome/Windows", Severity.INFO,
                "Salary budget report accessed by HR", yesterday.atTime(14, 0), false, null);

        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "UPDATE", "Increment", "INC-BULK", "20.0.0.8",
                "Chrome/Windows", Severity.INFO,
                "Bulk increment spreadsheet uploaded for FY 2026-27", yesterday.atTime(11, 30), false, null);

        createSeedLog("EMP009", "rahul@company.com", "Rahul Verma", "DELETE", "Payroll Settings", "TEMP-FY2023",
                "20.0.0.1", "Safari/Mac", Severity.INFO,
                "Archived payroll template 'FY2023-v3.xls' deleted", LocalDate.of(currentYear, 4, 4).atTime(15, 0),
                false, null);

        createSeedLog("EMP008", "ananya@company.com", "Ananya Das", "RUN", "Payroll", "DRY-RUN", "20.0.0.8",
                "Chrome/Windows", Severity.INFO,
                "Payroll dry run completed — no errors detected", LocalDate.of(currentYear, 4, 4).atTime(14, 30), false,
                null);

    }

    private void createSeedLog(String userId, String email, String name, String action, String module, String entityId,
            String ip, String device, Severity severity, String details, LocalDateTime time,
            boolean flagged, String flagReason) {
        AuditLog log = new AuditLog(userId, email, name, action, module, entityId, ip, device, severity, details);
        log.setCreatedAt(time);
        log.setFlagged(flagged);
        if (flagged) {
            log.setFlagReason(flagReason);
            log.setFlaggedAt(time);
        }
        auditLogRepository.save(log);
    }
}
