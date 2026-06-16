package com.example.ems.schedule.dto;

public class ChangeRequestPayload {

    private Long currentShiftId;
    private Long requestedShiftId;
    private String requestedDate; // "2026-06-20"
    private String requestType; // "SHIFT_CHANGE"
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
