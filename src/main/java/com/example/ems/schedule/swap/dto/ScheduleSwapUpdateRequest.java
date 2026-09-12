package com.example.ems.schedule.swap.dto;

public class ScheduleSwapUpdateRequest {

    private String targetScheduleId;
    private String reason;

    public ScheduleSwapUpdateRequest() {}

    public ScheduleSwapUpdateRequest(String targetScheduleId, String reason) {
        this.targetScheduleId = targetScheduleId;
        this.reason = reason;
    }

    public String getTargetScheduleId() { return targetScheduleId; }
    public void setTargetScheduleId(String targetScheduleId) { this.targetScheduleId = targetScheduleId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
