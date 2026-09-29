package com.example.ems.appraisal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Increments Report Summary")
public record IncrementsReportSummaryDto(
        @Schema(description = "Report Type", example = "ANNUAL")
        String reportType,

        @Schema(description = "Module Name", example = "Increments")
        String module,

        @Schema(description = "Generation Timestamp", example = "2026-09-09T10:00:00")
        LocalDateTime generatedAt,

        @Schema(description = "Total Applied Increments Count", example = "42")
        Integer totalAppliedIncrementsCount,

        @Schema(description = "Total Increment Budget Amount", example = "250000.00")
        BigDecimal totalIncrementBudgetAmount,

        @Schema(description = "Average Increment Percentage", example = "8.5")
        Double averageIncrementPercentage
) {}
