package com.example.ems.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for reviewing an audit log entry")
public record ReviewAuditLogRequest(
    @Schema(description = "Review remarks/notes", example = "Reviewed and verified as legitimate activity")
    String remarks
) {}
