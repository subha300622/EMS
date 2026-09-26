package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditDashboardStatsDto;
import com.example.ems.audit.dto.AuditLogDetailResponse;
import com.example.ems.audit.dto.AuditLogFilterRequest;
import com.example.ems.audit.dto.AuditLogResponse;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuditLogQueryService {

    Page<AuditLogResponse> getLogs(AuditLogFilterRequest request, User currentUser, Pageable pageable);

    AuditLogDetailResponse getLogById(Long id, User currentUser);

    List<AuditLogResponse> getEmployeeAuditHistory(String employeeId, User currentUser);

    Page<AuditLogResponse> getMyActivity(User currentUser, Pageable pageable);

    AuditDashboardStatsDto getDashboardStats(User currentUser);

    AuditDashboardStatsDto getDashboardStats(Collection<String> allowedModules);

    Page<AuditLog> getFilteredLogs(
            String search, String module, String action, String user,
            String date, LocalDateTime from, LocalDateTime to, Severity severity, Boolean flagged,
            Collection<String> allowedModules, Pageable pageable);

    AuditLog reviewLog(Long id, String reviewerUsername, String remarks);

    void dismissAllFlags(String reviewerUsername);

    Optional<AuditLog> findLogById(Long id);

    List<AuditLog> getLogsByUser(String userId);

    List<AuditLog> getLogsByEntity(String entityType, String entityId);

    List<AuditLog> getAllLogs();
}
