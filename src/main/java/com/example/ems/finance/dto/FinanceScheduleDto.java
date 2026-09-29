package com.example.ems.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Today's shift and attendance schedule for finance dashboard")
public record FinanceScheduleDto(
        @Schema(description = "Assigned shift name", example = "Morning Shift")
        String shiftName,

        @Schema(description = "Shift start time (HH:mm)", example = "06:00")
        String startTime,

        @Schema(description = "Shift end time (HH:mm)", example = "14:00")
        String endTime,

        @Schema(description = "Work location name", example = "HQ Office, Chennai")
        String location,

        @Schema(description = "Actual check-in time recorded today", example = "09:05")
        String checkInTime,

        @Schema(description = "Whether check-in has been verified", example = "true")
        Boolean checkInVerified
) {}
