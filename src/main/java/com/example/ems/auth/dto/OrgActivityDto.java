package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization Activity Feed Item")
public record OrgActivityDto(
        @Schema(description = "Timestamp of the activity", example = "2026-09-09T10:15:30Z")
        String timestamp,

        @Schema(description = "Action performed", example = "USER_LOGIN")
        String action,

        @Schema(description = "Detailed description", example = "User logged in successfully")
        String details,

        @Schema(description = "Actor user name or email", example = "admin@example.com")
        String actor
) {}
