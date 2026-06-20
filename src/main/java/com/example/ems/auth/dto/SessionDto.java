package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class SessionDto {
    @Schema(example = "string")
    private String sessionId;
    @Schema(example = "string")
    private String userAgent;
    @Schema(example = "123 Main St, Springfield")
    private String ipAddress;
    @Schema(example = "string")
    private String createdAt;

    public SessionDto() {}

    public SessionDto(String sessionId, String userAgent, String ipAddress, String createdAt) {
        this.sessionId = sessionId;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
