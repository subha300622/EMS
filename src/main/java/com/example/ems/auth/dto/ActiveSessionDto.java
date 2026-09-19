package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Active user session details")
public record ActiveSessionDto(
    @Schema(description = "Unique session ID", example = "sess-12345") String sessionId,
    @Schema(description = "Device information / User Agent", example = "Chrome/Mac") String device,
    @Schema(description = "IP address", example = "20.0.0.8") String ipAddress,
    @Schema(description = "Location", example = "Bangalore, India") String location,
    @Schema(description = "Session creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Last active timestamp") LocalDateTime lastActiveAt,
    @Schema(description = "Whether this is the current active session", example = "true") boolean current
) {}
