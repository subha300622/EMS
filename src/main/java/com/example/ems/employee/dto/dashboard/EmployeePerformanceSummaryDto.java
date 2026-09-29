package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Performance appraisal summary")
public record EmployeePerformanceSummaryDto(
        @Schema(description = "Overall performance rating score", example = "4.5")
        Double rating,

        @Schema(description = "Maximum scale rating score", example = "5.0")
        Double maxRating,

        @Schema(description = "Last review or appraisal date", example = "2025-12-01")
        String lastReviewDate,

        @Schema(description = "Performance rating label", example = "Excellent")
        String ratingLabel
) {
}
