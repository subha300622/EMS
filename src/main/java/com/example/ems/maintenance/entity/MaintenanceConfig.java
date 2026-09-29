package com.example.ems.maintenance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "platform_maintenance_config")
public class MaintenanceConfig {

    @Id
    private Long id = 1L;

    @jakarta.persistence.Version
    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(name = "allow_admin_access", nullable = false)
    private Boolean allowAdminAccess = true;

    @Column(length = 500)
    private String message;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    @Column(name = "logout_active_sessions", nullable = false)
    private Boolean logoutActiveSessions = false;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public MaintenanceConfig() {
    }

    public MaintenanceConfig(Long id, Boolean enabled, Boolean allowAdminAccess, String message,
                             OffsetDateTime startAt, OffsetDateTime endAt, Boolean logoutActiveSessions,
                             OffsetDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.enabled = enabled;
        this.allowAdminAccess = allowAdminAccess;
        this.message = message;
        this.startAt = startAt;
        this.endAt = endAt;
        this.logoutActiveSessions = logoutActiveSessions;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
