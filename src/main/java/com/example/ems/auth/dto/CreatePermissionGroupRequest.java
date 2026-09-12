package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "Request body for creating a permission group")
public record CreatePermissionGroupRequest(
    @Schema(description = "Unique code for permission group", example = "EMPLOYEE_MANAGEMENT")
    @NotBlank(message = "Code is required")
    String code,

    @Schema(description = "Human-readable name", example = "Employee Management")
    @NotBlank(message = "Name is required")
    String name,

    @Schema(description = "Description of group", example = "Permissions related to employee administration")
    String description,

    @Schema(description = "Initial permission IDs to include in the group", example = "[1, 2, 3]")
    List<Long> permissionIds
) {}
