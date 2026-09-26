package com.example.ems.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public class MaintenanceResponse {

    private boolean enabled;
    private boolean effective;
    private String status;
    private boolean allowAdminAccess;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    private boolean logoutActiveSessions;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime updatedAt;

    private String updatedBy;

    public MaintenanceResponse() {
    }

    public MaintenanceResponse(boolean enabled, boolean effective, String status, boolean allowAdminAccess,
                               String message, OffsetDateTime startAt, OffsetDateTime endAt,
                               boolean logoutActiveSessions, OffsetDateTime updatedAt, String updatedBy) {
        this.enabled = enabled;
        this.effective = effective;
        this.status = status;
        this.allowAdminAccess = allowAdminAccess;
        this.message = message;
        this.startAt = startAt;
        this.endAt = endAt;
        this.logoutActiveSessions = logoutActiveSessions;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEffective() {
        return effective;
    }

    public void setEffective(boolean effective) {
        this.effective = effective;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAllowAdminAccess() {
        return allowAdminAccess;
    }

    public void setAllowAdminAccess(boolean allowAdminAccess) {
        this.allowAdminAccess = allowAdminAccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public boolean isLogoutActiveSessions() {
        return logoutActiveSessions;
    }

    public void setLogoutActiveSessions(boolean logoutActiveSessions) {
        this.logoutActiveSessions = logoutActiveSessions;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
