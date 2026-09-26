package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating a permission group")
public record UpdatePermissionGroupRequest(
    @Schema(description = "Updated name", example = "Employee Administration")
    String name,

    @Schema(description = "Updated description", example = "Updated scope of permissions")
    String description
) {}
