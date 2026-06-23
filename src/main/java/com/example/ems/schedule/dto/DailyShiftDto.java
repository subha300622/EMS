package com.example.ems.schedule.dto;

import java.time.LocalDate;

public class DailyShiftDto {
    private LocalDate date;
    private Long shiftId;
    private String type; // MORNING, EVENING, NIGHT, FULL_DAY, LEAVE, NONE
    private String label; // "Morning", "Evening", "Night", "Full Day", "On Approved Leave", "None"
    private String timeRange; // e.g. "08:00 - 14:00"
    private String status; // ASSIGNED, COMPLETED, LEAVE

    public DailyShiftDto() {}

    public DailyShiftDto(LocalDate date, Long shiftId, String type, String label, String timeRange, String status) {
        this.date = date;
        this.shiftId = shiftId;
        this.type = type;
        this.label = label;
        this.timeRange = timeRange;
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
