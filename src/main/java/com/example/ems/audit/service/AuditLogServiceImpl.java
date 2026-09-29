package com.example.ems.audit.service;

import com.example.ems.audit.dto.AuditDashboardStatsDto;
import com.example.ems.audit.dto.AuditLogEvent;
import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.enums.AuditStatus;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.context.SecurityContextFacade;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditDataSanitizer auditDataSanitizer;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private SecurityContextFacade securityContextFacade;

    @Autowired
    @Lazy
    private AuditLogQueryService auditLogQueryService;

    @Autowired
    @Lazy
    private AuditLogExportService auditLogExportService;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuditLog success(AuditLogEvent event) {
        try {
            return recordLog(event, AuditStatus.SUCCESS, null);
        } catch (Exception e) {
            log.warn("Audit log (success) could not be persisted, business operation continues: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuditLog success(AuditModule module, AuditAction action, String entityType, String recordId,
            Object oldValue, Object newValue, String details) {
        AuditLogEvent event = AuditLogEvent.builder()
                .module(module)
                .action(action)
                .entityType(entityType)
                .recordId(recordId)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build();
        return success(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog failure(AuditLogEvent event) {
        return recordLog(event, AuditStatus.FAILED, event.getFailureReason());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog failure(AuditModule module, AuditAction action, String entityType, String recordId,
            Object oldValue, Object newValue, String failureReason) {
        AuditLogEvent event = AuditLogEvent.builder()
                .module(module)
                .action(action)
                .entityType(entityType)
                .recordId(recordId)
                .oldValue(oldValue)
                .newValue(newValue)
                .failureReason(failureReason)
                .details("Action failed: " + failureReason)
                .build();
        return failure(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog denied(AuditLogEvent event) {
        return recordLog(event, AuditStatus.DENIED, event.getFailureReason());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog recordSecurityAudit(com.example.ems.security.event.SecurityAuditEvent event) {
        if (event == null) {
            return null;
        }
        AuditLogEvent logEvent = AuditLogEvent.builder()
                .permission(event.permission())
                .action(event.action())
                .entityType(event.resourceType() != null ? event.resourceType() : "SECURITY")
                .recordId(event.resourceId() != null ? event.resourceId() : event.permission())
                .failureReason(event.failureReason())
                .details("Access Denied: "
                        + (event.failureReason() != null ? event.failureReason() : "Insufficient permission"))
                .build();
        return recordLog(logEvent, event.status() != null ? event.status() : AuditStatus.DENIED, event.failureReason());
    }

    private AuditLog recordLog(AuditLogEvent event, AuditStatus status, String failureReason) {
        try {
            ResolvedAuditContext context = resolveContext(event);

            String sanitizedOld = auditDataSanitizer.sanitizeToJson(event.getOldValue());
            String sanitizedNew = auditDataSanitizer.sanitizeToJson(event.getNewValue());

            AuditAction finalAction = event.getAction();
            if (finalAction == null) {
                finalAction = deriveActionFromHttpMethod(context.httpMethod);
            }
            String actionStr = finalAction != null ? finalAction.name() : "UNKNOWN";
            String moduleStr = event.getModule() != null ? event.getModule().name()
                    : (event.getEntityType() != null ? event.getEntityType().toUpperCase() : "SYSTEM");

            Severity severity;
            if (status == AuditStatus.DENIED) {
                severity = Severity.CRITICAL;
            } else if (status == AuditStatus.FAILED) {
                severity = Severity.WARNING;
            } else {
                severity = Severity.INFO;
            }

            AuditLog auditLog = AuditLog.builder()
                    .companyId(context.companyId)
                    .userId(context.userId)
                    .userEmail(context.userEmail)
                    .userName(context.userName)
                    .departmentId(context.departmentId)
                    .module(moduleStr)
                    .action(actionStr)
                    .entityType(event.getEntityType())
                    .recordId(event.getRecordId())
                    .oldValue(sanitizedOld)
                    .newValue(sanitizedNew)
                    .ipAddress(context.ipAddress)
                    .device(context.device)
                    .browser(context.browser)
                    .status(status)
                    .failureReason(failureReason)
                    .requestId(context.requestId)
                    .permission(event.getPermission())
                    .httpMethod(context.httpMethod)
                    .apiPath(context.apiPath)
                    .details(event.getDetails())
                    .severity(severity)
                    .createdAt(LocalDateTime.now())
                    .build();

            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AuditLog logAction(String userId, String userEmail, String action, String entityType, String entityId,
            String ipAddress, String details) {
        return logAction(userId, userEmail, null, action, entityType, entityId, ipAddress, null, Severity.INFO,
                details);
    }

    @Override
    public AuditLog logAction(String userId, String userEmail, String userName, String action, String entityType,
            String entityId, String ipAddress, String device, Severity severity, String details) {
        AuditLog log = new AuditLog(userId, userEmail, userName, action, entityType, entityId, ipAddress, device,
                severity != null ? severity : Severity.INFO, details);

        // Enrich with companyId from context if available
        Long companyId = TenantContext.getOrganizationId();
        if (companyId != null) {
            log = AuditLog.builder()
                    .companyId(companyId)
                    .userId(userId)
                    .userEmail(userEmail)
                    .userName(userName)
                    .action(action)
                    .entityType(entityType)
                    .recordId(entityId)
                    .ipAddress(ipAddress)
                    .device(device)
                    .severity(severity != null ? severity : Severity.INFO)
                    .details(details)
                    .module(entityType != null ? entityType.toUpperCase() : "SYSTEM")
                    .status(AuditStatus.SUCCESS)
                    .build();
        }
        return auditLogRepository.save(log);
    }

    // Context resolution helper
    private static class ResolvedAuditContext {
        Long companyId;
        String userId;
        String userEmail;
        String userName;
        Long departmentId;
        String ipAddress;
        String device;
        String browser;
        String requestId;
        String httpMethod;
        String apiPath;
    }

    private ResolvedAuditContext resolveContext(AuditLogEvent event) {
        ResolvedAuditContext ctx = new ResolvedAuditContext();

        // 1. Initial values from event
        ctx.companyId = event.getCompanyId();
        ctx.userId = event.getUserId();
        ctx.userEmail = event.getUserEmail();
        ctx.userName = event.getUserName();
        ctx.departmentId = event.getDepartmentId();
        ctx.ipAddress = event.getIpAddress();
        ctx.device = event.getDevice();
        ctx.browser = event.getBrowser();
        ctx.requestId = event.getRequestId();
        ctx.httpMethod = event.getHttpMethod();
        ctx.apiPath = event.getApiPath();

        // 2. Resolve company ID from TenantContext if absent
        if (ctx.companyId == null) {
            ctx.companyId = TenantContext.getOrganizationId();
        }

        // 3. Resolve user identity from SecurityContext if available
        if (ctx.userEmail == null || ctx.userId == null) {
            resolveSecurityIdentity(ctx);
        }

        // 4. Resolve request metadata (IP, Device, Browser, RequestId) from HTTP
        // request if present
        resolveRequestMetadata(ctx);

        // 5. Fallbacks for non-null requirements
        if (ctx.userEmail == null) {
            ctx.userEmail = "system@ems.internal";
        }
        if (ctx.userName == null) {
            ctx.userName = "SYSTEM";
        }
        if (ctx.userId == null) {
            ctx.userId = "SYSTEM";
        }

        return ctx;
    }

    private void resolveSecurityIdentity(ResolvedAuditContext ctx) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                Object principal = auth.getPrincipal();
                String email = null;
                if (principal instanceof AuthPrincipal ap) {
                    ctx.userId = ap.getUserId();
                    email = ap.getEmail();
                } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                    email = ud.getUsername();
                } else if (principal instanceof String str && !str.isBlank()) {
                    email = str;
                }

                if (email != null && !email.isBlank()) {
                    ctx.userEmail = email;
                    if (userRepository != null) {
                        User user = userRepository.findByWorkEmail(email).orElse(null);
                        if (user != null) {
                            if (ctx.userId == null)
                                ctx.userId = user.getUserId();
                            if (ctx.userName == null)
                                ctx.userName = user.getFullName();
                            if (ctx.departmentId == null)
                                ctx.departmentId = user.getDepartmentId();
                            if (ctx.companyId == null)
                                ctx.companyId = user.getOrganizationId();
                        }
                    }
                }
            } else if (securityContextFacade != null && securityContextFacade.isAuthenticated()) {
                ctx.userId = securityContextFacade.getUserId();
                ctx.userEmail = securityContextFacade.getEmail();
            }
        } catch (Exception e) {
            log.debug("Could not resolve security context for audit log: {}", e.getMessage());
        }
    }

    private void resolveRequestMetadata(ResolvedAuditContext ctx) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                if (ctx.ipAddress == null) {
                    String xForwardedFor = request.getHeader("X-Forwarded-For");
                    if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
                        ctx.ipAddress = xForwardedFor.split(",")[0].trim();
                    } else {
                        ctx.ipAddress = request.getRemoteAddr();
                    }
                }

                if (ctx.requestId == null) {
                    String reqId = request.getHeader("X-Request-ID");
                    if (reqId == null || reqId.trim().isEmpty()) {
                        reqId = MDC.get("correlationId");
                    }
                    ctx.requestId = reqId;
                }

                if (ctx.httpMethod == null) {
                    ctx.httpMethod = request.getMethod();
                }

                if (ctx.apiPath == null) {
                    ctx.apiPath = request.getRequestURI();
                }

                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && !userAgent.isBlank()) {
                    if (ctx.browser == null) {
                        ctx.browser = extractBrowser(userAgent);
                    }
                    if (ctx.device == null) {
                        ctx.device = extractDevice(userAgent);
                    }
                }
            } else {
                if (ctx.requestId == null) {
                    ctx.requestId = MDC.get("correlationId");
                }
            }
        } catch (Exception e) {
            log.debug("Could not resolve request attributes for audit log: {}", e.getMessage());
        }
    }

    private AuditAction deriveActionFromHttpMethod(String method) {
        if (method == null)
            return AuditAction.VIEW;
        return switch (method.toUpperCase()) {
            case "GET" -> AuditAction.VIEW;
            case "POST" -> AuditAction.CREATE;
            case "PUT", "PATCH" -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            default -> AuditAction.VIEW;
        };
    }

    private String extractBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("postman"))
            return "Postman";
        if (ua.contains("curl"))
            return "curl";
        if (ua.contains("edg"))
            return "Microsoft Edge";
        if (ua.contains("chrome"))
            return "Chrome";
        if (ua.contains("firefox"))
            return "Firefox";
        if (ua.contains("safari"))
            return "Safari";
        return "Unknown Browser";
    }

    private String extractDevice(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone"))
            return "Mobile";
        if (ua.contains("ipad") || ua.contains("tablet"))
            return "Tablet";
        if (ua.contains("macintosh") || ua.contains("mac os x"))
            return "Mac";
        if (ua.contains("windows"))
            return "Windows PC";
        if (ua.contains("linux"))
            return "Linux PC";
        return "Unknown Device";
    }

    // Delegations to query and export services for backward compatibility
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogQueryService.getAllLogs();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getFilteredLogs(
            String search, String module, String action, String user,
            String date, LocalDateTime from, LocalDateTime to, Severity severity, Boolean flagged,
            Collection<String> allowedModules, Pageable pageable) {
        return auditLogQueryService.getFilteredLogs(search, module, action, user, date, from, to, severity, flagged,
                allowedModules, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditDashboardStatsDto getDashboardStats(Collection<String> allowedModules) {
        return auditLogQueryService.getDashboardStats(allowedModules);
    }

    @Override
    public AuditLog reviewLog(Long id, String reviewerUsername, String remarks) {
        return auditLogQueryService.reviewLog(id, reviewerUsername, remarks);
    }

    @Override
    public void dismissAllFlags(String reviewerUsername) {
        auditLogQueryService.dismissAllFlags(reviewerUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> getLogById(Long id) {
        return auditLogQueryService.findLogById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(String userId) {
        return auditLogQueryService.getLogsByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, String entityId) {
        return auditLogQueryService.getLogsByEntity(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportLogsToCsv(Collection<String> allowedModules) {
        return auditLogExportService.exportLogsToCsv(allowedModules);
    }

    @Override
    public void seedAuditLogs() {
        // Seed logs if table is empty
        if (auditLogRepository.count() > 0) {
            return;
        }
        logAction("EMP008", "ananya@company.com", "Ananya Das", "RUN", "Payroll", "284", "20.0.0.8",
                "Chrome/Windows", Severity.INFO, "Payroll: April 2026 processing initiated — 284 employees");
    }
}
