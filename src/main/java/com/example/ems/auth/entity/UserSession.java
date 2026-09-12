package com.example.ems.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String email;

    @Column(length = 1024)
    private String userAgent;

    private String ipAddress;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private boolean isRevoked;

    @Column(nullable = false)
    private int sessionVersion = 1;

    @Column(nullable = false)
    private long sessionEpoch = 1L;

    @Column(nullable = false)
    private String status = "ACTIVE";

    private LocalDateTime revokedAt;

    private String revocationEventId;

    public UserSession() {}

    public UserSession(String sessionId, String userId, String email, String userAgent, String ipAddress, String refreshToken, LocalDateTime createdAt, LocalDateTime expiryTime, boolean isRevoked) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.email = email;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.refreshToken = refreshToken;
        this.createdAt = createdAt;
        this.expiryTime = expiryTime;
        this.isRevoked = isRevoked;
        this.sessionVersion = 1;
        this.sessionEpoch = 1L;
        this.status = "ACTIVE";
    }

    public UserSession(String sessionId, String userId, String email, String userAgent, String ipAddress, String refreshToken, LocalDateTime createdAt, LocalDateTime expiryTime, boolean isRevoked, int sessionVersion, long sessionEpoch, String status) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.email = email;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.refreshToken = refreshToken;
        this.createdAt = createdAt;
        this.expiryTime = expiryTime;
        this.isRevoked = isRevoked;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public boolean isRevoked() { return isRevoked; }
    public void setRevoked(boolean revoked) { isRevoked = revoked; }

    public int getSessionVersion() { return sessionVersion; }
    public void setSessionVersion(int sessionVersion) { this.sessionVersion = sessionVersion; }

    public long getSessionEpoch() { return sessionEpoch; }
    public void setSessionEpoch(long sessionEpoch) { this.sessionEpoch = sessionEpoch; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevocationEventId() { return revocationEventId; }
    public void setRevocationEventId(String revocationEventId) { this.revocationEventId = revocationEventId; }
}
