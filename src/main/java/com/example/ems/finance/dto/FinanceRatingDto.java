package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Performance rating summary for finance dashboard")
public record FinanceRatingDto(
        @Schema(description = "Overall performance rating score", example = "4.3")
        Double rating,

        @Schema(description = "Last performance review date (YYYY-MM-DD)", example = "2025-12-01")
        String lastReviewDate
) {}
