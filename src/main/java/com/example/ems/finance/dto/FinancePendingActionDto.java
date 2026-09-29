package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Pending actionable item for finance dashboard")
public record FinancePendingActionDto(
        @Schema(description = "Action category type", example = "LEAVE")
        String type,

        @Schema(description = "Action title", example = "Apply for Leave")
        String title,

        @Schema(description = "Action summary description or due info", example = "8 days remaining")
        String description,

        @Schema(description = "Action status", example = "ACTION_REQUIRED")
        String status,

        @Schema(description = "Frontend navigation destination link", example = "/leave/apply")
        String actionUrl
) {}
