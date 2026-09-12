package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for checking if user has a permission")
public record CheckPermissionRequest(
    @Schema(description = "Permission code to verify", example = "employee.read")
    @NotBlank(message = "Permission code is required")
    String permission
) {}
