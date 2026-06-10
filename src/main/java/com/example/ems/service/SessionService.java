package com.example.ems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class SessionMetadata {
        private String sessionId;
        private String userId;
        private String email;
        private String userAgent;
        private String ipAddress;
        private String refreshToken;
        private String createdAt;

        public SessionMetadata() {}

        public SessionMetadata(String sessionId, String userId, String email, String userAgent, String ipAddress, String refreshToken) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.email = email;
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
            this.refreshToken = refreshToken;
            this.createdAt = LocalDateTime.now().toString();
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
    }

    private String getTokenKey(String token) {
        return "session:token:" + token;
    }

    private String getUserSessionKey(String userId, String sessionId) {
        return "session:user:" + userId + ":" + sessionId;
    }

    public SessionMetadata createSession(String userId, String email, String userAgent, String ipAddress) {
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        SessionMetadata session = new SessionMetadata(sessionId, userId, email, userAgent, ipAddress, refreshToken);

        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(getTokenKey(refreshToken), json, REFRESH_TOKEN_VALIDITY);
            redisTemplate.opsForValue().set(getUserSessionKey(userId, sessionId), json, REFRESH_TOKEN_VALIDITY);
            log.info("Session created for user {}: SessionID={}", email, sessionId);
        } catch (Exception e) {
            log.error("Failed to save session to Redis", e);
        }

        return session;
    }

    public SessionMetadata getSessionByRefreshToken(String refreshToken) {
        String json = redisTemplate.opsForValue().get(getTokenKey(refreshToken));
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, SessionMetadata.class);
        } catch (Exception e) {
            log.error("Failed to parse session from Redis", e);
            return null;
        }
    }

    public SessionMetadata rotateRefreshToken(String oldRefreshToken) {
        SessionMetadata session = getSessionByRefreshToken(oldRefreshToken);
        if (session == null) return null;

        // Revoke old token key
        redisTemplate.delete(getTokenKey(oldRefreshToken));

        // Generate new refresh token
        String newRefreshToken = UUID.randomUUID().toString();
        session.setRefreshToken(newRefreshToken);

        try {
            String json = objectMapper.writeValueAsString(session);
            // Save new token key
            redisTemplate.opsForValue().set(getTokenKey(newRefreshToken), json, REFRESH_TOKEN_VALIDITY);
            // Update user session metadata key
            redisTemplate.opsForValue().set(getUserSessionKey(session.getUserId(), session.getSessionId()), json, REFRESH_TOKEN_VALIDITY);
            log.info("Token rotated for session ID: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to rotate refresh token in Redis", e);
            return null;
        }

        return session;
    }

    public void revokeSession(String refreshToken) {
        SessionMetadata session = getSessionByRefreshToken(refreshToken);
        if (session != null) {
            redisTemplate.delete(getTokenKey(refreshToken));
            redisTemplate.delete(getUserSessionKey(session.getUserId(), session.getSessionId()));
            log.info("Session revoked: {}", session.getSessionId());
        }
    }

    public void revokeSessionById(String userId, String sessionId) {
        String userKey = getUserSessionKey(userId, sessionId);
        String json = redisTemplate.opsForValue().get(userKey);
        if (json != null) {
            try {
                SessionMetadata session = objectMapper.readValue(json, SessionMetadata.class);
                redisTemplate.delete(getTokenKey(session.getRefreshToken()));
                redisTemplate.delete(userKey);
                log.info("Session ID {} revoked for User {}", sessionId, userId);
            } catch (Exception e) {
                log.error("Failed to revoke session by ID", e);
            }
        }
    }

    public List<SessionMetadata> getActiveSessions(String userId) {
        List<SessionMetadata> list = new ArrayList<>();
        Set<String> keys = redisTemplate.keys("session:user:" + userId + ":*");
        if (keys == null || keys.isEmpty()) return list;

        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                try {
                    list.add(objectMapper.readValue(json, SessionMetadata.class));
                } catch (Exception e) {
                    log.error("Failed to parse active session key {}", key, e);
                }
            }
        }
        return list;
    }

    public void revokeAllSessions(String userId) {
        Set<String> keys = redisTemplate.keys("session:user:" + userId + ":*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                try {
                    SessionMetadata session = objectMapper.readValue(json, SessionMetadata.class);
                    redisTemplate.delete(getTokenKey(session.getRefreshToken()));
                } catch (Exception e) {
                    log.error("Failed to revoke refresh token key in revokeAllSessions", e);
                }
            }
            redisTemplate.delete(key);
        }
        log.info("All sessions revoked for user: {}", userId);
    }
}
