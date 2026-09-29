package com.example.ems.schedule.dto;

import com.example.ems.schedule.entity.ScheduleStatus;

public class ScheduleDto {
    private String scheduleId;
    private String employeeId;
    private String employeeName;
    private String date;
    private String startTime;
    private String endTime;
    private ScheduleStatus status;
    private String location;
    private String notes;

    public ScheduleDto() {}

    public ScheduleDto(String scheduleId, String employeeId, String employeeName, String date, String startTime, String endTime, ScheduleStatus status, String location, String notes) {
        this.scheduleId = scheduleId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.location = location;
        this.notes = notes;
    }

    // Getters and Setters
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

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
