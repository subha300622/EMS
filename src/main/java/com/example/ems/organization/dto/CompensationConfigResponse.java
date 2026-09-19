package com.example.ems.organization.dto;

import com.example.ems.organization.entity.OrganizationCompensationConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CompensationConfigResponse {

    private Long organizationId;
    private boolean overtimeEnabled;
    private boolean incentiveEnabled;
    private boolean bonusEnabled;
    private LocalDateTime updatedAt;

    public CompensationConfigResponse() {}

    public CompensationConfigResponse(Long organizationId, boolean overtimeEnabled, boolean incentiveEnabled, boolean bonusEnabled) {
        this(organizationId, overtimeEnabled, incentiveEnabled, bonusEnabled, null);
    }

    public CompensationConfigResponse(Long organizationId, boolean overtimeEnabled, boolean incentiveEnabled, boolean bonusEnabled, LocalDateTime updatedAt) {
        this.organizationId = organizationId;
        this.overtimeEnabled = overtimeEnabled;
        this.incentiveEnabled = incentiveEnabled;
        this.bonusEnabled = bonusEnabled;
        this.updatedAt = updatedAt;
    }

    public static CompensationConfigResponse fromEntity(OrganizationCompensationConfig config) {
        if (config == null) return null;
        CompensationConfigResponse response = new CompensationConfigResponse();
        response.setOrganizationId(config.getOrganization() != null ? config.getOrganization().getId() : null);
        response.setOvertimeEnabled(config.isOvertimeEnabled());
        response.setIncentiveEnabled(config.isIncentiveEnabled());
        response.setBonusEnabled(config.isBonusEnabled());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }

    public static CompensationConfigResponse defaults(Long organizationId) {
        CompensationConfigResponse response = new CompensationConfigResponse();
        response.setOrganizationId(organizationId);
        response.setOvertimeEnabled(true);
        response.setIncentiveEnabled(true);
        response.setBonusEnabled(true);
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    @JsonProperty("overtimeEnabled")
    public boolean isOvertimeEnabled() {
        return overtimeEnabled;
    }

    public void setOvertimeEnabled(boolean overtimeEnabled) {
        this.overtimeEnabled = overtimeEnabled;
    }

    @JsonProperty("incentiveEnabled")
    public boolean isIncentiveEnabled() {
        return incentiveEnabled;
    }

    public void setIncentiveEnabled(boolean incentiveEnabled) {
        this.incentiveEnabled = incentiveEnabled;
    }

    @JsonProperty("bonusEnabled")
    public boolean isBonusEnabled() {
        return bonusEnabled;
    }

    public void setBonusEnabled(boolean bonusEnabled) {
        this.bonusEnabled = bonusEnabled;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
