package com.example.ems.audit.entity;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;
import com.example.ems.audit.enums.AuditStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_company_id", columnList = "company_id"),
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_module", columnList = "module"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_status", columnList = "status"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_logs_department_id", columnList = "department_id")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "module")
    private String module;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "record_id")
    private String recordId;

    @Column(name = "entity_id")
    private String entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device")
    private String device;

    @Column(name = "browser")
    private String browser;

    @Column(name = "status")
    private String status = AuditStatus.SUCCESS.name();

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "permission")
    private String permission;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "api_path")
    private String apiPath;

    // Legacy fields preserved for backward compatibility
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity = Severity.INFO;

    @Column(name = "flagged")
    private Boolean flagged = false;

    @Column(name = "flag_reason")
    private String flagReason;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    // Backwards compatibility constructor (7 params)
    public AuditLog(String userId, String userEmail, String action, String entityType, String entityId, String ipAddress, String details) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.recordId = entityId;
        this.module = entityType != null ? entityType.toUpperCase() : AuditModule.SYSTEM.name();
        this.ipAddress = ipAddress;
        this.details = details;
        this.status = AuditStatus.SUCCESS.name();
        this.createdAt = LocalDateTime.now();
        this.severity = Severity.INFO;
        this.flagged = false;
    }

    // Extended legacy constructor (10 params)
    public AuditLog(String userId, String userEmail, String userName, String action, String entityType, String entityId, String ipAddress, String device, Severity severity, String details) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.recordId = entityId;
        this.module = entityType != null ? entityType.toUpperCase() : AuditModule.SYSTEM.name();
        this.ipAddress = ipAddress;
        this.device = device;
        this.severity = severity != null ? severity : Severity.INFO;
        this.details = details;
        this.status = AuditStatus.SUCCESS.name();
        this.createdAt = LocalDateTime.now();
        this.flagged = false;
    }

    // Full enterprise builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuditLog log = new AuditLog();

        public Builder companyId(Long companyId) { log.companyId = companyId; return this; }
        public Builder userId(String userId) { log.userId = userId; return this; }
        public Builder userEmail(String userEmail) { log.userEmail = userEmail; return this; }
        public Builder userName(String userName) { log.userName = userName; return this; }
        public Builder departmentId(Long departmentId) { log.departmentId = departmentId; return this; }
        public Builder module(AuditModule module) { log.module = module != null ? module.name() : null; return this; }
        public Builder module(String module) { log.module = module; return this; }
        public Builder action(AuditAction action) { log.action = action != null ? action.name() : null; return this; }
        public Builder action(String action) { log.action = action; return this; }
        public Builder entityType(String entityType) { log.entityType = entityType; return this; }
        public Builder recordId(String recordId) { 
            log.recordId = recordId; 
            log.entityId = recordId; 
            return this; 
        }
        public Builder oldValue(String oldValue) { log.oldValue = oldValue; return this; }
        public Builder newValue(String newValue) { log.newValue = newValue; return this; }
        public Builder ipAddress(String ipAddress) { log.ipAddress = ipAddress; return this; }
        public Builder device(String device) { log.device = device; return this; }
        public Builder browser(String browser) { log.browser = browser; return this; }
        public Builder status(AuditStatus status) { log.status = status != null ? status.name() : AuditStatus.SUCCESS.name(); return this; }
        public Builder status(String status) { log.status = status; return this; }
        public Builder failureReason(String failureReason) { log.failureReason = failureReason; return this; }
        public Builder requestId(String requestId) { log.requestId = requestId; return this; }
        public Builder permission(String permission) { log.permission = permission; return this; }
        public Builder httpMethod(String httpMethod) { log.httpMethod = httpMethod; return this; }
        public Builder apiPath(String apiPath) { log.apiPath = apiPath; return this; }
        public Builder severity(Severity severity) { log.severity = severity != null ? severity : Severity.INFO; return this; }
        public Builder details(String details) { log.details = details; return this; }
        public Builder createdAt(LocalDateTime createdAt) { 
            if (createdAt != null) log.createdAt = createdAt; 
            return this; 
        }

        public AuditLog build() {
            if (log.status == null) log.status = AuditStatus.SUCCESS.name();
            if (log.severity == null) log.severity = Severity.INFO;
            if (log.flagged == null) log.flagged = false;
            if (log.recordId == null && log.entityId != null) log.recordId = log.entityId;
            if (log.entityId == null && log.recordId != null) log.entityId = log.recordId;
            if (log.module == null && log.entityType != null) log.module = log.entityType.toUpperCase();
            return log;
        }
    }

    // Getters
    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public Long getDepartmentId() { return departmentId; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getRecordId() { return recordId != null ? recordId : entityId; }
    public String getEntityId() { return entityId != null ? entityId : recordId; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getIpAddress() { return ipAddress; }
    public String getDevice() { return device; }
    public String getBrowser() { return browser; }
    public String getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public String getRequestId() { return requestId; }
    public Severity getSeverity() { return severity; }
    public Boolean getFlagged() { return flagged; }
    public String getFlagReason() { return flagReason; }
    public LocalDateTime getFlaggedAt() { return flaggedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Legacy setters for backwards compatibility with tests / seeders
    public void setId(Long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setAction(String action) { this.action = action; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public void setEntityId(String entityId) { 
        this.entityId = entityId; 
        if (this.recordId == null) this.recordId = entityId;
    }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setDevice(String device) { this.device = device; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setFlagged(Boolean flagged) { this.flagged = flagged; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
    public void setFlaggedAt(LocalDateTime flaggedAt) { this.flaggedAt = flaggedAt; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setDetails(String details) { this.details = details; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
}
