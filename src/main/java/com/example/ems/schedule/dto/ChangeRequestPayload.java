package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ChangeRequestPayload {

    @Schema(example = "1")
    private Long currentShiftId;
    @Schema(example = "1")
    private Long requestedShiftId;
    @Schema(example = "string")
    private String requestedDate; // "2026-06-20"
    @Schema(example = "string")
    private String requestType; // "SHIFT_CHANGE"
    @Schema(example = "Personal business")
    private String reason;

    // Getters and Setters
    public Long getCurrentShiftId() { return currentShiftId; }
    public void setCurrentShiftId(Long currentShiftId) { this.currentShiftId = currentShiftId; }

    public Long getRequestedShiftId() { return requestedShiftId; }
    public void setRequestedShiftId(Long requestedShiftId) { this.requestedShiftId = requestedShiftId; }

    public String getRequestedDate() { return requestedDate; }
    public void setRequestedDate(String requestedDate) { this.requestedDate = requestedDate; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
