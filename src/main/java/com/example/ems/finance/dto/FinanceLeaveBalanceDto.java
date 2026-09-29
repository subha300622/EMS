package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Leave balance details for finance dashboard")
public record FinanceLeaveBalanceDto(
        @Schema(description = "Total remaining leave days across all categories", example = "12")
        Integer totalRemaining,

        @Schema(description = "Detailed breakdown by leave type code", example = "{\"CL\": 6, \"EL\": 4, \"SL\": 2}")
        Map<String, Integer> balances
) {}
