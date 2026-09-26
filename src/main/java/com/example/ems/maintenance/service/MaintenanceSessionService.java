package com.example.ems.maintenance.service;

import com.example.ems.auth.service.SafeRedisService;
import com.example.ems.auth.service.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MaintenanceSessionService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceSessionService.class);
    public static final String REDIS_MAINTENANCE_REVOCATION_KEY = "maintenance:session_revocation_epoch";

    @Autowired(required = false)
    private SafeRedisService safeRedisService;

    /**
     * Injects the DB-backed session store explicitly.
     * Two SessionStore beans exist (databaseSessionStore, redisSessionCache).
     * Bulk revocation must go through the DB store — Redis cache is a read-through
     * cache that does not own session state.
     */
    @Autowired
    @Qualifier("databaseSessionStore")
    private SessionStore sessionStore;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void invalidateActiveSessions() {
        long revocationEpoch = System.currentTimeMillis();
        log.info("Invalidating active user sessions due to maintenance mode activation at epoch {}", revocationEpoch);

        // 1. Set global revocation marker in Redis
        if (safeRedisService != null) {
            try {
                safeRedisService.set(REDIS_MAINTENANCE_REVOCATION_KEY, String.valueOf(revocationEpoch), java.time.Duration.ofDays(7));
            } catch (Exception e) {
                log.warn("Failed to set maintenance session revocation marker in Redis: {}", e.getMessage());
            }
        }

        // 2. Revoke active sessions via SessionStore (backed by DatabaseSessionStore → UserSessionRepository)
        try {
            int revokedCount = sessionStore.revokeAllActiveSessions(LocalDateTime.now());
            log.info("Revoked {} active database sessions due to maintenance activation", revokedCount);
        } catch (Exception e) {
            log.error("Failed to revoke database sessions on maintenance activation: {}", e.getMessage(), e);
        }
    }

    public boolean isSessionRevokedByMaintenance(long sessionEpoch) {
        if (safeRedisService != null) {
            try {
                String marker = safeRedisService.get(REDIS_MAINTENANCE_REVOCATION_KEY);
                if (marker != null) {
                    long revocationEpoch = Long.parseLong(marker);
                    return sessionEpoch < revocationEpoch;
                }
            } catch (Exception e) {
                log.warn("Error reading maintenance revocation epoch from Redis: {}", e.getMessage());
            }
        }
        return false;
    }
}
