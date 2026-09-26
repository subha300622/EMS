package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Details of an attendance break interval")
public class AttendanceBreakDto {

    @Schema(description = "Break record identifier", example = "42")
    private Long id;

    @Schema(description = "Break start timestamp in ISO-8601 UTC", example = "2026-09-10T13:00:00Z")
    private Instant breakStartTime;

    @Schema(description = "Break end timestamp in ISO-8601 UTC (null if break is ongoing)", example = "2026-09-10T13:30:00Z")
    private Instant breakEndTime;

    @Schema(description = "Duration in minutes (null if break is ongoing)", example = "30")
    private Integer durationMinutes;

    @Schema(description = "Whether this break is currently ongoing", example = "false")
    private boolean active;

    public AttendanceBreakDto() {}

    public AttendanceBreakDto(Long id, Instant breakStartTime, Instant breakEndTime, Integer durationMinutes, boolean active) {
        this.id = id;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
        this.durationMinutes = durationMinutes;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(Instant breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public Instant getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(Instant breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
