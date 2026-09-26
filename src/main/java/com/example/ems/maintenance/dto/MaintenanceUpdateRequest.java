package com.example.ems.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class MaintenanceUpdateRequest {

    @NotNull(message = "enabled is required")
    private Boolean enabled;

    @NotNull(message = "allowAdminAccess is required")
    private Boolean allowAdminAccess;

    @Size(max = 500, message = "message cannot exceed 500 characters")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    private Boolean logoutActiveSessions = false;

    public MaintenanceUpdateRequest() {
    }

    public MaintenanceUpdateRequest(Boolean enabled, Boolean allowAdminAccess, String message,
                                  OffsetDateTime startAt, OffsetDateTime endAt, Boolean logoutActiveSessions) {
        this.enabled = enabled;
        this.allowAdminAccess = allowAdminAccess;
        this.message = message;
        this.startAt = startAt;
        this.endAt = endAt;
        this.logoutActiveSessions = logoutActiveSessions;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAllowAdminAccess() {
        return allowAdminAccess;
    }

    public void setAllowAdminAccess(Boolean allowAdminAccess) {
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

    public Boolean getLogoutActiveSessions() {
        return logoutActiveSessions;
    }

    public void setLogoutActiveSessions(Boolean logoutActiveSessions) {
        this.logoutActiveSessions = logoutActiveSessions;
    }
}
