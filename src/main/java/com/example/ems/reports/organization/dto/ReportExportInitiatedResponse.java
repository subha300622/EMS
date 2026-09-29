package com.example.ems.reports.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Platform report export triggered response")
public record ReportExportInitiatedResponse(
        @Schema(description = "Export History Record ID", example = "1") Long exportId,
        @Schema(description = "Export Job Status", example = "PENDING") String status,
        @Schema(description = "Direct Download URL when complete", example = "/api/v1/platform/reports/organizations/export/download/1") String downloadUrl
) {}
