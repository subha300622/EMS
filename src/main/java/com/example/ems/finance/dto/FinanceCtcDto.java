package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CTC compensation summary for finance dashboard")
public record FinanceCtcDto(
        @Schema(description = "Annual Cost to Company", example = "1200000.00")
        BigDecimal annualCtc,

        @Schema(description = "Formatted display value", example = "₹12L")
        String displayValue,

        @Schema(description = "Effective from date (YYYY-MM-DD)", example = "2026-01-01")
        String effectiveFrom,

        @Schema(description = "Currency code", example = "INR")
        String currency
) {}
