package com.example.ems.maintenance.service;

import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.entity.MaintenanceStatus;
import com.example.ems.security.dto.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class MaintenanceEvaluator {

    public MaintenanceStatus calculateStatus(MaintenanceConfig config, OffsetDateTime now) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return MaintenanceStatus.MAINTENANCE_DISABLED;
        }

        if (config.getStartAt() != null && now.isBefore(config.getStartAt())) {
            return MaintenanceStatus.MAINTENANCE_SCHEDULED;
        }

        if (config.getEndAt() != null && !now.isBefore(config.getEndAt())) {
            return MaintenanceStatus.MAINTENANCE_EXPIRED;
        }

        return MaintenanceStatus.MAINTENANCE_ENABLED;
    }

    public boolean isMaintenanceActive(MaintenanceConfig config, OffsetDateTime now) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return false;
        }

        if (config.getStartAt() != null && now.isBefore(config.getStartAt())) {
            return false;
        }

        if (config.getEndAt() != null && !now.isBefore(config.getEndAt())) {
            return false;
        }

        return true;
    }

    public boolean canAccessDuringMaintenance(MaintenanceConfig config, Authentication authentication, OffsetDateTime now) {
        if (!isMaintenanceActive(config, now)) {
            return true;
        }

        if (!Boolean.TRUE.equals(config.getAllowAdminAccess())) {
            return false;
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Platform-level authorization check: Only PLATFORM_ADMIN is allowed to bypass
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthPrincipal authPrincipal) {
            if ("PLATFORM_ADMIN".equalsIgnoreCase(authPrincipal.getRole())) {
                return true;
            }
        }

        return authentication.getAuthorities().stream().anyMatch(auth -> {
            String authority = auth.getAuthority();
            return "ROLE_PLATFORM_ADMIN".equalsIgnoreCase(authority)
                    || "PLATFORM_ADMIN".equalsIgnoreCase(authority)
                    || "platform.maintenance.manage".equalsIgnoreCase(authority)
                    || "PLATFORM_MAINTENANCE_MANAGE".equalsIgnoreCase(authority);
        });
    }

    public void validateUpdateRequest(MaintenanceUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (Boolean.TRUE.equals(request.getEnabled())) {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("message is required when maintenance mode is enabled");
            }
        }

        if (request.getStartAt() != null && request.getEndAt() != null) {
            if (!request.getEndAt().isAfter(request.getStartAt())) {
                throw new IllegalArgumentException("endAt must be strictly after startAt");
            }
        }
    }

    public void validateEnableRequest(MaintenanceEnableRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("message is required when enabling maintenance mode");
        }

        if (request.getStartAt() != null && request.getEndAt() != null) {
            if (!request.getEndAt().isAfter(request.getStartAt())) {
                throw new IllegalArgumentException("endAt must be strictly after startAt");
            }
        }
    }
}
