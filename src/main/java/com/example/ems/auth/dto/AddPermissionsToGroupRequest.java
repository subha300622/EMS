package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request body for adding permissions to a permission group")
public record AddPermissionsToGroupRequest(
    @Schema(description = "List of Permission IDs to add to group", example = "[1, 2, 4]")
    @NotEmpty(message = "permissionIds cannot be empty")
    List<Long> permissionIds
) {}
