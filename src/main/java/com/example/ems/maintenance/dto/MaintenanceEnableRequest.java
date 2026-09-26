package com.example.ems.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class MaintenanceEnableRequest {

    @NotBlank(message = "message is required when enabling maintenance")
    @Size(max = 500, message = "message cannot exceed 500 characters")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    private Boolean allowAdminAccess = true;

    private Boolean logoutActiveSessions = false;

    public MaintenanceEnableRequest() {
    }

    public MaintenanceEnableRequest(String message, OffsetDateTime startAt, OffsetDateTime endAt,
                                  Boolean allowAdminAccess, Boolean logoutActiveSessions) {
        this.message = message;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allowAdminAccess = allowAdminAccess != null ? allowAdminAccess : true;
        this.logoutActiveSessions = logoutActiveSessions != null ? logoutActiveSessions : false;
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

    public Boolean getAllowAdminAccess() {
        return allowAdminAccess;
    }

    public void setAllowAdminAccess(Boolean allowAdminAccess) {
        this.allowAdminAccess = allowAdminAccess;
    }

    public Boolean getLogoutActiveSessions() {
        return logoutActiveSessions;
    }

    public void setLogoutActiveSessions(Boolean logoutActiveSessions) {
        this.logoutActiveSessions = logoutActiveSessions;
    }
}
