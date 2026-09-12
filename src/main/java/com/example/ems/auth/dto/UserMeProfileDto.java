package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Logged in user profile and metadata")
public record UserMeProfileDto(
    @Schema(description = "User internal ID", example = "1") Long id,
    @Schema(description = "Employee ID", example = "EMP001") String employeeId,
    @Schema(description = "Full name", example = "John Doe") String name,
    @Schema(description = "Work email", example = "john@example.com") String email,
    @Schema(description = "User role details") RoleInfo role,
    @Schema(description = "Effective permissions list") List<String> permissions,
    @Schema(description = "Organization ID", example = "1") Long organizationId,
    @Schema(description = "Organization name", example = "Acme Corp") String organizationName,
    @Schema(description = "Branch location", example = "HQ") String branch
) {
    @Schema(description = "Role information")
    public record RoleInfo(
        @Schema(description = "Role ID", example = "1") Long roleId,
        @Schema(description = "Role name", example = "ADMIN") String name
    ) {}
}
