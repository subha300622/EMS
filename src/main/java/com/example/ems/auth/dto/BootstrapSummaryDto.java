package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "User and organization bootstrap summary")
public record BootstrapSummaryDto(
    @Schema(description = "User basic information") UserSummaryDto user,
    @Schema(description = "Organization information") OrgSummaryDto organization,
    @Schema(description = "Assigned roles") List<String> roles
) {
    @Schema(description = "User summary")
    public record UserSummaryDto(
        @Schema(description = "User ID", example = "USR-001") String userId,
        @Schema(description = "Employee ID", example = "EMP001") String employeeId,
        @Schema(description = "Full name", example = "John Doe") String fullName,
        @Schema(description = "Email", example = "john@example.com") String email,
        @Schema(description = "Account status", example = "ACTIVE") String status
    ) {}

    @Schema(description = "Organization summary")
    public record OrgSummaryDto(
        @Schema(description = "Organization ID", example = "1") String organizationId,
        @Schema(description = "Organization name", example = "Acme Corp") String name
    ) {}
}
