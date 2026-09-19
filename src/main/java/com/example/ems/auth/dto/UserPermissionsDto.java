package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "User roles and permissions summary")
public record UserPermissionsDto(
    @Schema(description = "Assigned roles") List<RoleSummaryDto> roles,
    @Schema(description = "Effective permissions list", example = "[\"employee.read\", \"attendance.read\"]") List<String> permissions
) {
    @Schema(description = "Role summary")
    public record RoleSummaryDto(
        @Schema(description = "Role ID", example = "1") Long id,
        @Schema(description = "Role name", example = "ADMIN") String name
    ) {}
}
