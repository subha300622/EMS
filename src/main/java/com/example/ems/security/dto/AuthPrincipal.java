package com.example.ems.security.dto;

public class AuthPrincipal {
    private final String userId;
    private final String sessionId;
    private final int sessionVersion;
    private final long sessionEpoch;
    private final String email;
    private final String role;

    public AuthPrincipal(String userId, String sessionId, int sessionVersion, long sessionEpoch, String email, String role) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.sessionVersion = sessionVersion;
        this.sessionEpoch = sessionEpoch;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getSessionVersion() {
        return sessionVersion;
    }

    public long getSessionEpoch() {
        return sessionEpoch;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "AuthPrincipal{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", sessionVersion=" + sessionVersion +
                ", sessionEpoch=" + sessionEpoch +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
