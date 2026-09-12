package com.example.ems.appraisal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Appraisal Batch Generation Response")
public record AppraisalBatchGenerationResponseDto(
        @Schema(description = "Appraisal Cycle ID", example = "101")
        Long cycleId,

        @Schema(description = "Appraisal Cycle Name", example = "Annual Performance Review 2026")
        String cycleName,

        @Schema(description = "Number of new appraisals generated", example = "25")
        Integer generatedCount,

        @Schema(description = "Total number of appraisals in cycle", example = "30")
        Integer totalAppraisalsInCycle
) {}
