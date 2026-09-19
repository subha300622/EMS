package com.example.ems.auth.service;

import com.example.ems.auth.entity.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RedisSessionCache implements SessionStore {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionCache.class);
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    @Autowired
    private SafeRedisService safeRedisService;

    private final ObjectMapper objectMapper;

    public RedisSessionCache() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private String getSessionIdKey(String sessionId) {
        return "session:id:" + sessionId;
    }

    private String getRefreshTokenKey(String refreshToken) {
        return "session:token:" + refreshToken;
    }

    @Override
    public void save(UserSession session) {
        if (session == null) return;
        try {
            String json = objectMapper.writeValueAsString(session);
            safeRedisService.set(getSessionIdKey(session.getSessionId()), json, CACHE_TTL);
            safeRedisService.set(getRefreshTokenKey(session.getRefreshToken()), json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to write to Redis cache for session {}: {}", session.getSessionId(), e.getMessage());
        }
    }

    @Override
    public Optional<UserSession> findById(String sessionId) {
        if (sessionId == null) return Optional.empty();
        try {
            String json = safeRedisService.get(getSessionIdKey(sessionId));
            if (json != null) {
                return Optional.of(objectMapper.readValue(json, UserSession.class));
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis cache for session ID {}: {}", sessionId, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        if (refreshToken == null) return Optional.empty();
        try {
            String json = safeRedisService.get(getRefreshTokenKey(refreshToken));
            if (json != null) {
                return Optional.of(objectMapper.readValue(json, UserSession.class));
            }
        } catch (Exception e) {
            log.warn("Failed to read from Redis cache for refresh token: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<UserSession> findByUserIdAndIsRevokedFalse(String userId) {
        // Caching active session list is not needed; query DB directly
        return Collections.emptyList();
    }

    @Override
    public void delete(String sessionId) {
        if (sessionId == null) return;
        try {
            Optional<UserSession> sessionOpt = findById(sessionId);
            safeRedisService.delete(getSessionIdKey(sessionId));
            sessionOpt.ifPresent(session -> safeRedisService.delete(getRefreshTokenKey(session.getRefreshToken())));
        } catch (Exception e) {
            log.warn("Failed to delete from Redis cache for session ID {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public Optional<UserSession> findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(String sessionId, String status, int sessionVersion, long sessionEpoch) {
        // DB is the only authority for validation; cache does not validate
        return Optional.empty();
    }
}
