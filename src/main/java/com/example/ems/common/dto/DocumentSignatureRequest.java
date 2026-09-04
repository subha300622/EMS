package com.example.ems.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for document signature actions (requesting signature or completing signature)")
public record DocumentSignatureRequest(
    @Schema(description = "Target employee ID (used when requesting a signature)", example = "101")
    Long employeeId,

    @Schema(description = "Signature status update (e.g. SIGNED, REJECTED) (used when completing a signature)", example = "SIGNED")
    String status,

    @Schema(description = "Comments or instructions related to the signature", example = "Please sign and return")
    String comments
) {}
