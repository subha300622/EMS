package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request body for assigning permission groups to a role")
public record AssignPermissionGroupsRequest(
    @Schema(description = "List of Permission Group IDs to assign", example = "[1, 2, 3]")
    @NotEmpty(message = "permissionGroupIds list cannot be empty")
    List<Long> permissionGroupIds
) {}
