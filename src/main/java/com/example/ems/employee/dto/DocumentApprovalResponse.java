package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Document approval action response")
public record DocumentApprovalResponse(
    @Schema(description = "Document ID", example = "101") Long documentId,
    @Schema(description = "Updated approval status", example = "APPROVED") String status,
    @Schema(description = "Review comments", example = "Document verified") String comment
) {}
