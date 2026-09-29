package com.example.ems.maintenance.service;

import com.example.ems.audit.enums.AuditAction;
import com.example.ems.audit.service.AuditLogService;
import com.example.ems.maintenance.entity.MaintenanceConfig;
import com.example.ems.maintenance.entity.MaintenanceStatus;
import com.example.ems.maintenance.event.MaintenanceScheduleEndedEvent;
import com.example.ems.maintenance.event.MaintenanceScheduleStartedEvent;
import com.example.ems.maintenance.repository.MaintenanceConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private final MaintenanceConfigRepository repository;
    private final MaintenanceEvaluator evaluator;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    private final AtomicReference<MaintenanceStatus> lastCalculatedStatus = new AtomicReference<>();

    public MaintenanceScheduler(MaintenanceConfigRepository repository,
                                MaintenanceEvaluator evaluator,
                                ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.evaluator = evaluator;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 30000)
    public void monitorScheduledMaintenance() {
        try {
            repository.findDefaultConfig().ifPresent(config -> {
                if (!Boolean.TRUE.equals(config.getEnabled())) {
                    lastCalculatedStatus.set(MaintenanceStatus.MAINTENANCE_DISABLED);
                    return;
                }

                OffsetDateTime now = OffsetDateTime.now();
                MaintenanceStatus currentStatus = evaluator.calculateStatus(config, now);
                MaintenanceStatus previousStatus = lastCalculatedStatus.getAndSet(currentStatus);

                if (previousStatus != null && previousStatus != currentStatus) {
                    if (previousStatus == MaintenanceStatus.MAINTENANCE_SCHEDULED && currentStatus == MaintenanceStatus.MAINTENANCE_ENABLED) {
                        log.info("Scheduled maintenance window started: {}", config.getMessage());
                        eventPublisher.publishEvent(new MaintenanceScheduleStartedEvent(config.getMessage(), config.getStartAt(), config.getEndAt()));
                        recordAudit(AuditAction.MAINTENANCE_SCHEDULE_STARTED, config);
                    } else if (previousStatus == MaintenanceStatus.MAINTENANCE_ENABLED && currentStatus == MaintenanceStatus.MAINTENANCE_EXPIRED) {
                        log.info("Scheduled maintenance window ended: {}", config.getMessage());
                        eventPublisher.publishEvent(new MaintenanceScheduleEndedEvent(config.getMessage(), config.getStartAt(), config.getEndAt()));
                        recordAudit(AuditAction.MAINTENANCE_SCHEDULE_ENDED, config);
                    }
                }
            });
        } catch (Exception e) {
            log.warn("Error running scheduled maintenance monitor: {}", e.getMessage());
        }
    }

    private void recordAudit(AuditAction action, MaintenanceConfig config) {
        if (auditLogService != null) {
            try {
                auditLogService.logAction(
                        "SYSTEM",
                        "system@ems.com",
                        action.name(),
                        "PLATFORM_MAINTENANCE",
                        String.valueOf(config.getId()),
                        "127.0.0.1",
                        "Scheduled maintenance event triggered: " + action.name()
                );
            } catch (Exception e) {
                log.warn("Failed to record scheduled maintenance audit log: {}", e.getMessage());
            }
        }
    }
}
