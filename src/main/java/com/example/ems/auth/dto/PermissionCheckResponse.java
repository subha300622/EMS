package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Permission check result")
public record PermissionCheckResponse(
    @Schema(description = "Permission evaluated", example = "employee.read") String permission,
    @Schema(description = "Whether the user holds the permission", example = "true") boolean allowed
) {}
