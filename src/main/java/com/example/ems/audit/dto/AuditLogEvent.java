package com.example.ems.audit.dto;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.enums.AuditModule;

public class AuditLogEvent {

    private Long companyId;
    private String userId;
    private String userName;
    private String userEmail;
    private Long departmentId;
    private AuditModule module;
    private AuditAction action;
    private String entityType;
    private String recordId;
    private Object oldValue;
    private Object newValue;
    private String ipAddress;
    private String device;
    private String browser;
    private String requestId;
    private String failureReason;
    private String details;
    private String permission;
    private String httpMethod;
    private String apiPath;

    public AuditLogEvent() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuditLogEvent event = new AuditLogEvent();

        public Builder companyId(Long companyId) { event.companyId = companyId; return this; }
        public Builder userId(String userId) { event.userId = userId; return this; }
        public Builder userName(String userName) { event.userName = userName; return this; }
        public Builder userEmail(String userEmail) { event.userEmail = userEmail; return this; }
        public Builder departmentId(Long departmentId) { event.departmentId = departmentId; return this; }
        public Builder module(AuditModule module) { event.module = module; return this; }
        public Builder action(AuditAction action) { event.action = action; return this; }
        public Builder entityType(String entityType) { event.entityType = entityType; return this; }
        public Builder recordId(String recordId) { event.recordId = recordId; return this; }
        public Builder oldValue(Object oldValue) { event.oldValue = oldValue; return this; }
        public Builder newValue(Object newValue) { event.newValue = newValue; return this; }
        public Builder ipAddress(String ipAddress) { event.ipAddress = ipAddress; return this; }
        public Builder device(String device) { event.device = device; return this; }
        public Builder browser(String browser) { event.browser = browser; return this; }
        public Builder requestId(String requestId) { event.requestId = requestId; return this; }
        public Builder failureReason(String failureReason) { event.failureReason = failureReason; return this; }
        public Builder details(String details) { event.details = details; return this; }
        public Builder permission(String permission) { event.permission = permission; return this; }
        public Builder httpMethod(String httpMethod) { event.httpMethod = httpMethod; return this; }
        public Builder apiPath(String apiPath) { event.apiPath = apiPath; return this; }

        public AuditLogEvent build() {
            return event;
        }
    }

    // Getters and Setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public Object getOldValue() { return oldValue; }
    public void setOldValue(Object oldValue) { this.oldValue = oldValue; }

    public Object getNewValue() { return newValue; }
    public void setNewValue(Object newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
}
