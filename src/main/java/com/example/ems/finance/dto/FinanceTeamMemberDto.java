package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Finance department colleague profile for team directory")
public record FinanceTeamMemberDto(
        @Schema(description = "Employee Database Identifier", example = "101")
        Long employeeId,

        @Schema(description = "Employee initials", example = "RK")
        String initials,

        @Schema(description = "Full name of the team member", example = "Rajan Kumar")
        String name,

        @Schema(description = "Job designation title", example = "Finance Lead")
        String designation,

        @Schema(description = "Availability status", example = "ONLINE")
        String availability
) {}
