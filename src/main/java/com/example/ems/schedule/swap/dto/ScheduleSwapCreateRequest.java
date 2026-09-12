package com.example.ems.schedule.swap.dto;

public class ScheduleSwapCreateRequest {

    private String sourceScheduleId;
    private String targetScheduleId;
    private String reason;

    public ScheduleSwapCreateRequest() {}

    public ScheduleSwapCreateRequest(String sourceScheduleId, String targetScheduleId, String reason) {
        this.sourceScheduleId = sourceScheduleId;
        this.targetScheduleId = targetScheduleId;
        this.reason = reason;
    }

    public String getSourceScheduleId() { return sourceScheduleId; }
    public void setSourceScheduleId(String sourceScheduleId) { this.sourceScheduleId = sourceScheduleId; }

    public String getTargetScheduleId() { return targetScheduleId; }
    public void setTargetScheduleId(String targetScheduleId) { this.targetScheduleId = targetScheduleId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
