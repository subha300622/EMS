package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditDashboardStatsDto;
import com.example.ems.audit.dto.AuditLogEvent;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuditLogService {

    /**
     * Records a successful audit event in the current transaction.
     */
    AuditLog success(AuditLogEvent event);

    AuditLog success(AuditModule module, AuditAction action, String entityType, String recordId,
                     Object oldValue, Object newValue, String details);

    /**
     * Records a failed audit event in an independent REQUIRES_NEW transaction so it commits
     * even if the outer business transaction rolls back.
     */
    AuditLog failure(AuditLogEvent event);

    AuditLog failure(AuditModule module, AuditAction action, String entityType, String recordId,
                     Object oldValue, Object newValue, String failureReason);

    /**
     * Records an access denied security audit event in an independent REQUIRES_NEW transaction.
     */
    AuditLog denied(AuditLogEvent event);

    AuditLog recordSecurityAudit(com.example.ems.security.event.SecurityAuditEvent event);

    /**
     * Legacy compatibility method (7 params).
     */
    AuditLog logAction(String userId, String userEmail, String action, String entityType, String entityId,
                      String ipAddress, String details);

    /**
     * Legacy compatibility method (10 params).
     */
    AuditLog logAction(String userId, String userEmail, String userName, String action, String entityType,
                      String entityId, String ipAddress, String device, Severity severity, String details);

    // Legacy query & admin operations delegated for backward compatibility
    List<AuditLog> getAllLogs();

    Page<AuditLog> getFilteredLogs(
            String search, String module, String action, String user,
            String date, LocalDateTime from, LocalDateTime to, Severity severity, Boolean flagged,
            Collection<String> allowedModules, Pageable pageable);

    AuditDashboardStatsDto getDashboardStats(Collection<String> allowedModules);

    AuditLog reviewLog(Long id, String reviewerUsername, String remarks);

    void dismissAllFlags(String reviewerUsername);

    Optional<AuditLog> getLogById(Long id);

    List<AuditLog> getLogsByUser(String userId);

    List<AuditLog> getLogsByEntity(String entityType, String entityId);

    byte[] exportLogsToCsv(Collection<String> allowedModules);

    void seedAuditLogs();
}
