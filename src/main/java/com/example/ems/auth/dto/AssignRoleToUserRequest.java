package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for assigning role to user/employee")
public record AssignRoleToUserRequest(
    @Schema(description = "User or Employee ID (numeric ID or UUID string)", example = "101")
    String userId,

    @Schema(description = "Alternative ID field for backwards compatibility", example = "101")
    String id
) {
    public String getEffectiveUserId() {
        if (userId != null && !userId.isBlank()) {
            return userId.trim();
        }
        if (id != null && !id.isBlank()) {
            return id.trim();
        }
        return null;
    }
}
