package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Payload for submitting an attendance regularization / correction request")
public class CreateRegularizationRequest {

    @NotNull(message = "attendanceId is mandatory")
    @Schema(description = "ID of the attendance session to regularize", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long attendanceId;

    @Schema(description = "Requested check-in timestamp in ISO-8601 UTC (optional if only correcting checkout)", example = "2026-09-10T09:00:00Z")
    private Instant requestedCheckInTime;

    @Schema(description = "Requested check-out timestamp in ISO-8601 UTC (optional if only correcting checkin)", example = "2026-09-10T18:00:00Z")
    private Instant requestedCheckOutTime;

    @NotBlank(message = "reason is mandatory")
    @Size(max = 500, message = "reason cannot exceed 500 characters")
    @Schema(description = "Reason for attendance correction", example = "Forgot to swipe out at office gate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    public CreateRegularizationRequest() {}

    public CreateRegularizationRequest(Long attendanceId, Instant requestedCheckInTime, Instant requestedCheckOutTime, String reason) {
        this.attendanceId = attendanceId;
        this.requestedCheckInTime = requestedCheckInTime;
        this.requestedCheckOutTime = requestedCheckOutTime;
        this.reason = reason;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Instant getRequestedCheckInTime() {
        return requestedCheckInTime;
    }

    public void setRequestedCheckInTime(Instant requestedCheckInTime) {
        this.requestedCheckInTime = requestedCheckInTime;
    }

    public Instant getRequestedCheckOutTime() {
        return requestedCheckOutTime;
    }

    public void setRequestedCheckOutTime(Instant requestedCheckOutTime) {
        this.requestedCheckOutTime = requestedCheckOutTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
