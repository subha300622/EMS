package com.example.ems.maintenance.service;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.maintenance.dto.MaintenanceBlockedResponse;
import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceResponse;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.dto.PublicMaintenanceResponse;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.entity.MaintenanceStatus;
import com.example.ems.maintenance.event.MaintenanceDisabledEvent;
import com.example.ems.maintenance.event.MaintenanceEnabledEvent;
import com.example.ems.maintenance.repository.MaintenanceConfigRepository;
import com.example.ems.security.dto.AuthPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceServiceImpl.class);

    private final MaintenanceConfigRepository repository;
    private final MaintenanceEvaluator evaluator;
    private final MaintenanceSessionService sessionService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    // In-memory cache for high-throughput filter checks
    private final AtomicReference<CachedConfig> configCache = new AtomicReference<>();
    private static final long CACHE_TTL_MILLIS = 3000L; // 3 seconds TTL

    private record CachedConfig(MaintenanceConfig config, long cachedAt) {}

    public MaintenanceServiceImpl(MaintenanceConfigRepository repository,
                                  MaintenanceEvaluator evaluator,
                                  MaintenanceSessionService sessionService,
                                  ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.evaluator = evaluator;
        this.sessionService = sessionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceResponse getSettings() {
        MaintenanceConfig config = getCurrentConfig();
        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceStatus status = evaluator.calculateStatus(config, now);
        boolean effective = evaluator.isMaintenanceActive(config, now);

        return toResponse(config, status, effective);
    }

    @Override
    @Transactional
    public MaintenanceResponse updateSettings(MaintenanceUpdateRequest request) {
        evaluator.validateUpdateRequest(request);

        MaintenanceConfig config = repository.findDefaultConfig()
                .orElseGet(() -> new MaintenanceConfig(1L, false, true, null, null, null, false, OffsetDateTime.now(), "SYSTEM"));

        boolean previousEnabled = Boolean.TRUE.equals(config.getEnabled());
        String currentActor = resolveCurrentActor();

        config.setEnabled(request.getEnabled());
        config.setAllowAdminAccess(request.getAllowAdminAccess());
        config.setMessage(request.getMessage());
        config.setStartAt(request.getStartAt());
        config.setEndAt(request.getEndAt());
        config.setLogoutActiveSessions(Boolean.TRUE.equals(request.getLogoutActiveSessions()));
        config.setUpdatedAt(OffsetDateTime.now());
        config.setUpdatedBy(currentActor);

        MaintenanceConfig saved = repository.save(config);
        invalidateCache();

        if (Boolean.TRUE.equals(saved.getLogoutActiveSessions()) && Boolean.TRUE.equals(saved.getEnabled())) {
            sessionService.invalidateActiveSessions();
        }

        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceStatus status = evaluator.calculateStatus(saved, now);
        boolean effective = evaluator.isMaintenanceActive(saved, now);

        AuditAction action = AuditAction.MAINTENANCE_UPDATED;
        if (!previousEnabled && Boolean.TRUE.equals(saved.getEnabled())) {
            action = AuditAction.MAINTENANCE_ENABLED;
        } else if (previousEnabled && !Boolean.TRUE.equals(saved.getEnabled())) {
            action = AuditAction.MAINTENANCE_DISABLED;
        }
        recordAudit(action, saved, currentActor);

        if (Boolean.TRUE.equals(saved.getEnabled())) {
            eventPublisher.publishEvent(new MaintenanceEnabledEvent(
                    saved.getAllowAdminAccess(),
                    saved.getLogoutActiveSessions(),
                    saved.getMessage(),
                    saved.getStartAt(),
                    saved.getEndAt(),
                    currentActor
            ));
        } else {
            eventPublisher.publishEvent(new MaintenanceDisabledEvent(currentActor));
        }

        return toResponse(saved, status, effective);
    }

    @Override
    @Transactional
    public MaintenanceResponse enable(MaintenanceEnableRequest request) {
        evaluator.validateEnableRequest(request);

        MaintenanceConfig config = repository.findDefaultConfig()
                .orElseGet(() -> new MaintenanceConfig(1L, false, true, null, null, null, false, OffsetDateTime.now(), "SYSTEM"));

        String currentActor = resolveCurrentActor();

        config.setEnabled(true);
        config.setMessage(request.getMessage());
        config.setStartAt(request.getStartAt());
        config.setEndAt(request.getEndAt());
        config.setAllowAdminAccess(request.getAllowAdminAccess() != null ? request.getAllowAdminAccess() : true);
        config.setLogoutActiveSessions(Boolean.TRUE.equals(request.getLogoutActiveSessions()));
        config.setUpdatedAt(OffsetDateTime.now());
        config.setUpdatedBy(currentActor);

        MaintenanceConfig saved = repository.save(config);
        invalidateCache();

        if (Boolean.TRUE.equals(saved.getLogoutActiveSessions())) {
            sessionService.invalidateActiveSessions();
        }

        OffsetDateTime now = OffsetDateTime.now();
        MaintenanceStatus status = evaluator.calculateStatus(saved, now);
        boolean effective = evaluator.isMaintenanceActive(saved, now);

        recordAudit(AuditAction.MAINTENANCE_ENABLED, saved, currentActor);

        eventPublisher.publishEvent(new MaintenanceEnabledEvent(
                saved.getAllowAdminAccess(),
                saved.getLogoutActiveSessions(),
                saved.getMessage(),
                saved.getStartAt(),
                saved.getEndAt(),
                currentActor
        ));

        return toResponse(saved, status, effective);
    }

    @Override
    @Transactional
    public MaintenanceResponse disable() {
        MaintenanceConfig config = repository.findDefaultConfig()
                .orElseGet(() -> new MaintenanceConfig(1L, false, true, null, null, null, false, OffsetDateTime.now(), "SYSTEM"));

        String currentActor = resolveCurrentActor();

        config.setEnabled(false);
        config.setUpdatedAt(OffsetDateTime.now());
        config.setUpdatedBy(currentActor);

        MaintenanceConfig saved = repository.save(config);
        invalidateCache();

        recordAudit(AuditAction.MAINTENANCE_DISABLED, saved, currentActor);
        eventPublisher.publishEvent(new MaintenanceDisabledEvent(currentActor));

        MaintenanceResponse response = toResponse(saved, MaintenanceStatus.MAINTENANCE_DISABLED, false);
        response.setMessage("Maintenance mode disabled.");
        return response;
    }

    @Override
    public boolean isMaintenanceActive() {
        MaintenanceConfig config = getCurrentConfig();
        return evaluator.isMaintenanceActive(config, OffsetDateTime.now());
    }

    @Override
    public boolean canAccessDuringMaintenance(Authentication authentication) {
        MaintenanceConfig config = getCurrentConfig();
        return evaluator.canAccessDuringMaintenance(config, authentication, OffsetDateTime.now());
    }

    @Override
    public PublicMaintenanceResponse getPublicStatus() {
        MaintenanceConfig config = getCurrentConfig();
        OffsetDateTime now = OffsetDateTime.now();
        boolean active = evaluator.isMaintenanceActive(config, now);

        if (!active) {
            return new PublicMaintenanceResponse(false);
        }

        return new PublicMaintenanceResponse(
                true,
                config.getMessage() != null ? config.getMessage() : "The EMS platform is currently under maintenance.",
                config.getStartAt(),
                config.getEndAt()
        );
    }

    @Override
    public MaintenanceConfig getCurrentConfig() {
        CachedConfig cached = configCache.get();
        long now = System.currentTimeMillis();
        if (cached != null && (now - cached.cachedAt()) < CACHE_TTL_MILLIS) {
            return cached.config();
        }

        MaintenanceConfig config = repository.findDefaultConfig()
                .orElseGet(() -> new MaintenanceConfig(1L, false, true,
                        "The EMS platform is currently under maintenance.", null, null, false,
                        OffsetDateTime.now(), "SYSTEM"));

        configCache.set(new CachedConfig(config, now));
        return config;
    }

    @Override
    public MaintenanceBlockedResponse getBlockedResponse() {
        MaintenanceConfig config = getCurrentConfig();
        return new MaintenanceBlockedResponse(
                "The EMS platform is currently under maintenance.",
                config.getMessage(),
                config.getStartAt(),
                config.getEndAt()
        );
    }

    private void invalidateCache() {
        configCache.set(null);
    }

    private MaintenanceResponse toResponse(MaintenanceConfig config, MaintenanceStatus status, boolean effective) {
        return new MaintenanceResponse(
                Boolean.TRUE.equals(config.getEnabled()),
                effective,
                status != null ? status.name() : MaintenanceStatus.MAINTENANCE_DISABLED.name(),
                Boolean.TRUE.equals(config.getAllowAdminAccess()),
                config.getMessage(),
                config.getStartAt(),
                config.getEndAt(),
                Boolean.TRUE.equals(config.getLogoutActiveSessions()),
                config.getUpdatedAt(),
                config.getUpdatedBy()
        );
    }

    private String resolveCurrentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof AuthPrincipal p && p.getEmail() != null) {
                return p.getEmail();
            }
            if (auth.getName() != null && !auth.getName().isBlank()) {
                return auth.getName();
            }
        }
        return "platform-admin";
    }

    private void recordAudit(AuditAction action, MaintenanceConfig config, String actor) {
        if (auditLogService != null) {
            try {
                String details = String.format("Maintenance config updated: enabled=%s, allowAdminAccess=%s, startAt=%s, endAt=%s, logoutActiveSessions=%s",
                        config.getEnabled(), config.getAllowAdminAccess(), config.getStartAt(), config.getEndAt(), config.getLogoutActiveSessions());
                auditLogService.logAction(
                        actor,
                        actor,
                        action.name(),
                        "PLATFORM_MAINTENANCE",
                        String.valueOf(config.getId()),
                        "0.0.0.0",
                        details
                );
            } catch (Exception e) {
                log.warn("Failed to record maintenance audit log: {}", e.getMessage());
            }
        }
    }
}
