package com.example.ems.auth.service;

import com.example.ems.auth.entity.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

    @Autowired
    private DatabaseSessionStore databaseSessionStore;

    @Autowired
    private RedisSessionCache redisSessionCache;

    public static class SessionMetadata {
        private String sessionId;
        private String userId;
        private String email;
        private String userAgent;
        private String ipAddress;
        private String refreshToken;
        private String createdAt;
        private int sessionVersion;
        private long sessionEpoch;
        private String status;
        private boolean current;

        public SessionMetadata() {}

        public SessionMetadata(String sessionId, String userId, String email, String userAgent, String ipAddress, String refreshToken) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.email = email;
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
            this.refreshToken = refreshToken;
            this.createdAt = LocalDateTime.now().toString();
            this.sessionVersion = 1;
            this.sessionEpoch = System.currentTimeMillis();
            this.status = "ACTIVE";
        }

        public SessionMetadata(String sessionId, String userId, String email, String userAgent, String ipAddress, String refreshToken, int sessionVersion, long sessionEpoch, String status) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.email = email;
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
            this.refreshToken = refreshToken;
            this.createdAt = LocalDateTime.now().toString();
            this.sessionVersion = sessionVersion;
            this.sessionEpoch = sessionEpoch;
            this.status = status;
        }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public int getSessionVersion() { return sessionVersion; }
        public void setSessionVersion(int sessionVersion) { this.sessionVersion = sessionVersion; }

        public long getSessionEpoch() { return sessionEpoch; }
        public void setSessionEpoch(long sessionEpoch) { this.sessionEpoch = sessionEpoch; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isCurrent() { return current; }
        public void setCurrent(boolean current) { this.current = current; }
    }

    private SessionMetadata convertToMetadata(UserSession userSession) {
        SessionMetadata metadata = new SessionMetadata();
        metadata.setSessionId(userSession.getSessionId());
        metadata.setUserId(userSession.getUserId());
        metadata.setEmail(userSession.getEmail());
        metadata.setUserAgent(userSession.getUserAgent());
        metadata.setIpAddress(userSession.getIpAddress());
        metadata.setRefreshToken(userSession.getRefreshToken());
        metadata.setCreatedAt(userSession.getCreatedAt().toString());
        metadata.setSessionVersion(userSession.getSessionVersion());
        metadata.setSessionEpoch(userSession.getSessionEpoch());
        metadata.setStatus(userSession.getStatus());
        return metadata;
    }

    @Transactional
    public SessionMetadata createSession(String userId, String email, String userAgent, String ipAddress) {
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        UserSession dbSession = new UserSession(
                sessionId,
                userId,
                email,
                userAgent,
                ipAddress,
                refreshToken,
                LocalDateTime.now(),
                LocalDateTime.now().plus(REFRESH_TOKEN_VALIDITY),
                false,
                1,
                1L,
                "ACTIVE"
        );

        // 1. Authoritative DB Write (transactional — rolls back if save fails)
        databaseSessionStore.save(dbSession);
        log.info("Session created in DB for user {}: SessionID={}", email, sessionId);

        // 2. Cache Write-Through (after DB save succeeds)
        redisSessionCache.save(dbSession);

        return convertToMetadata(dbSession);
    }

    public SessionMetadata getSessionByRefreshToken(String refreshToken) {
        if (refreshToken == null) return null;

        // 1. Try Redis cache first
        Optional<UserSession> cachedSession = redisSessionCache.findByRefreshToken(refreshToken);
        if (cachedSession.isPresent()) {
            return convertToMetadata(cachedSession.get());
        }

        // 2. Query DB on cache miss
        Optional<UserSession> dbSessionOpt = databaseSessionStore.findByRefreshToken(refreshToken);
        if (dbSessionOpt.isPresent()) {
            UserSession dbSession = dbSessionOpt.get();
            if (!dbSession.isRevoked() && "ACTIVE".equals(dbSession.getStatus()) && LocalDateTime.now().isBefore(dbSession.getExpiryTime())) {
                // Populate cache
                redisSessionCache.save(dbSession);
                return convertToMetadata(dbSession);
            }
        }
        return null;
    }

    @Transactional
    public SessionMetadata rotateRefreshToken(String oldRefreshToken) {
        Optional<UserSession> dbSessionOpt = databaseSessionStore.findByRefreshToken(oldRefreshToken);
        if (dbSessionOpt.isEmpty()) {
            log.warn("rotateRefreshToken: Token not found in DB — may belong to a previous server session or was already consumed");
            return null;
        }

        UserSession dbSession = dbSessionOpt.get();

        if (dbSession.isRevoked() || dbSession.getRevokedAt() != null) {
            log.warn("rotateRefreshToken: Session {} is already REVOKED (revokedAt={})",
                    dbSession.getSessionId(), dbSession.getRevokedAt());
            return null;
        }
        if (!"ACTIVE".equals(dbSession.getStatus())) {
            log.warn("rotateRefreshToken: Session {} has non-ACTIVE status: '{}'",
                    dbSession.getSessionId(), dbSession.getStatus());
            return null;
        }
        if (LocalDateTime.now().isAfter(dbSession.getExpiryTime())) {
            log.warn("rotateRefreshToken: Session {} is EXPIRED (expiryTime={})",
                    dbSession.getSessionId(), dbSession.getExpiryTime());
            return null;
        }

        String newRefreshToken = UUID.randomUUID().toString();

        // 1. Update session in DB within this transaction (DB is the authority)
        dbSession.setRefreshToken(newRefreshToken);
        dbSession.setSessionVersion(dbSession.getSessionVersion() + 1);
        dbSession.setSessionEpoch(dbSession.getSessionEpoch() + 1);
        databaseSessionStore.save(dbSession);

        // 2. Evict old cache entry and write new entry AFTER DB save succeeds
        redisSessionCache.delete(dbSession.getSessionId());
        redisSessionCache.save(dbSession);

        log.info("Token rotated for session ID: {}, new version: {}, new epoch: {}",
                dbSession.getSessionId(), dbSession.getSessionVersion(), dbSession.getSessionEpoch());
        return convertToMetadata(dbSession);
    }

    @Transactional
    public void revokeSession(String refreshToken) {
        if (refreshToken == null) return;

        Optional<UserSession> dbSessionOpt = databaseSessionStore.findByRefreshToken(refreshToken);
        if (dbSessionOpt.isPresent()) {
            UserSession dbSession = dbSessionOpt.get();
            dbSession.setRevoked(true);
            dbSession.setStatus("REVOKED");
            dbSession.setRevokedAt(LocalDateTime.now());
            dbSession.setRevocationEventId(UUID.randomUUID().toString());
            // Scramble refresh token so it cannot be matched/found by old value anymore
            dbSession.setRefreshToken("REVOKED-" + java.util.UUID.randomUUID().toString());
            databaseSessionStore.save(dbSession);
            // Evict from Redis cache after DB commit
            redisSessionCache.delete(dbSession.getSessionId());
            log.info("Session revoked: {}", dbSession.getSessionId());
        } else {
            log.warn("revokeSession: Refresh token not found in DB — session may have already been revoked");
        }
    }

    @Transactional
    public void revokeSessionById(String userId, String sessionId) {
        if (sessionId == null) return;

        Optional<UserSession> dbSessionOpt = databaseSessionStore.findById(sessionId);
        if (dbSessionOpt.isPresent()) {
            UserSession dbSession = dbSessionOpt.get();
            dbSession.setRevoked(true);
            dbSession.setStatus("REVOKED");
            dbSession.setRevokedAt(LocalDateTime.now());
            dbSession.setRevocationEventId(UUID.randomUUID().toString());
            // Scramble refresh token so it cannot be matched/found by old value anymore
            dbSession.setRefreshToken("REVOKED-" + java.util.UUID.randomUUID().toString());
            databaseSessionStore.save(dbSession);
            // Evict from Redis cache after DB save
            redisSessionCache.delete(sessionId);
            log.info("Session ID {} revoked in DB for User {}", sessionId, userId);
        } else {
            log.warn("revokeSessionById: Session ID {} not found in DB for user {}", sessionId, userId);
        }
    }

    public List<SessionMetadata> getActiveSessions(String userId) {
        List<SessionMetadata> list = new ArrayList<>();
        if (userId == null) return list;

        List<UserSession> dbSessions = databaseSessionStore.findByUserIdAndIsRevokedFalse(userId);
        for (UserSession dbSession : dbSessions) {
            if ("ACTIVE".equals(dbSession.getStatus()) && LocalDateTime.now().isBefore(dbSession.getExpiryTime())) {
                list.add(convertToMetadata(dbSession));
            }
        }
        return list;
    }

    @Transactional
    public void revokeAllSessions(String userId) {
        if (userId == null) return;

        List<UserSession> dbSessions = databaseSessionStore.findByUserIdAndIsRevokedFalse(userId);
        for (UserSession dbSession : dbSessions) {
            dbSession.setRevoked(true);
            dbSession.setStatus("REVOKED");
            dbSession.setRevokedAt(LocalDateTime.now());
            dbSession.setRevocationEventId(UUID.randomUUID().toString());
            // Scramble refresh token so it cannot be matched/found by old value anymore
            dbSession.setRefreshToken("REVOKED-" + java.util.UUID.randomUUID().toString());
            databaseSessionStore.save(dbSession);
            // Evict from Redis cache after DB save
            redisSessionCache.delete(dbSession.getSessionId());
        }
        log.info("All {} sessions revoked in DB for user: {}", dbSessions.size(), userId);
    }

    public boolean isSessionActive(String userId, String sessionId) {
        // Validation check is now performed exclusively in SessionAuthorityService, 
        // but delegate to the DatabaseSessionStore directly here for compatibility.
        if (userId == null || sessionId == null) {
            return false;
        }
        Optional<UserSession> dbSessionOpt = databaseSessionStore.findById(sessionId);
        if (dbSessionOpt.isPresent()) {
            UserSession dbSession = dbSessionOpt.get();
            return !dbSession.isRevoked() && "ACTIVE".equals(dbSession.getStatus()) && LocalDateTime.now().isBefore(dbSession.getExpiryTime());
        }
        return false;
    }
}
