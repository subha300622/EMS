package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exit request decision response")
public record ExitRequestDecisionDto(
        @Schema(description = "Exit request ID", example = "101")
        Long requestId,

        @Schema(description = "Exit decision status", example = "APPROVED")
        String status,

        @Schema(description = "Decision comments / notes", example = "Resignation approved by manager")
        String comment
) {}
