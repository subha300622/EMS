package com.example.ems.attendance.dto.adjustment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "Request payload for creating an attendance adjustment")
public class CreateAdjustmentRequest {

    @Schema(description = "Requested new check-in timestamp (UTC)", example = "2026-09-10T09:05:00Z")
    private Instant requestedCheckInTime;

    @Schema(description = "Requested new check-out timestamp (UTC)", example = "2026-09-10T18:00:00Z")
    private Instant requestedCheckOutTime;

    @NotBlank(message = "Reason is mandatory")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Schema(description = "Reason for HR/Admin attendance adjustment", example = "Biometric device was unavailable")
    private String reason;

    public CreateAdjustmentRequest() {}

    public CreateAdjustmentRequest(Instant requestedCheckInTime, Instant requestedCheckOutTime, String reason) {
        this.requestedCheckInTime = requestedCheckInTime;
        this.requestedCheckOutTime = requestedCheckOutTime;
        this.reason = reason;
    }

    public Instant getRequestedCheckInTime() { return requestedCheckInTime; }
    public void setRequestedCheckInTime(Instant requestedCheckInTime) { this.requestedCheckInTime = requestedCheckInTime; }

    public Instant getRequestedCheckOutTime() { return requestedCheckOutTime; }
    public void setRequestedCheckOutTime(Instant requestedCheckOutTime) { this.requestedCheckOutTime = requestedCheckOutTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
