package com.example.ems.schedule.dto;

import com.example.ems.schedule.entity.ScheduleStatus;

public class ScheduleUpdateRequest {
    private String date;
    private String startTime;
    private String endTime;
    private ScheduleStatus status;
    private String location;
    private String notes;

    public ScheduleUpdateRequest() {}

    public ScheduleUpdateRequest(String date, String startTime, String endTime, ScheduleStatus status, String location, String notes) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.location = location;
        this.notes = notes;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
