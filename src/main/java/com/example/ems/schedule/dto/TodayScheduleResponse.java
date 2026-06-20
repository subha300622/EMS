package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class TodayScheduleResponse {

    @Schema(example = "string")
    private String date;
    private ShiftInfo shift;
    private List<TodayEvent> events;
    @Schema(example = "ACTIVE")
    private String workingStatus; // WORKING, OFF, LEAVE, HOLIDAY

    public static class ShiftInfo {
        @Schema(example = "1")
        private Long shiftId;
        @Schema(example = "string")
        private String name;
        @Schema(example = "string")
        private String startTime;
        @Schema(example = "string")
        private String endTime;
        @Schema(example = "1")
        private Integer breakDurationMinutes;
        @Schema(example = "Bangalore")
        private String location;

        // Getters and Setters
        public Long getShiftId() { return shiftId; }
        public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Integer getBreakDurationMinutes() { return breakDurationMinutes; }
        public void setBreakDurationMinutes(Integer breakDurationMinutes) { this.breakDurationMinutes = breakDurationMinutes; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class TodayEvent {
        @Schema(example = "1")
        private Long eventId;
        @Schema(example = "string")
        private String time;
        @Schema(example = "Project Deliverables")
        private String title;
        @Schema(example = "string")
        private String type; // MEETING, SHIFT, LEAVE, etc.

        public TodayEvent() {}

        public TodayEvent(Long eventId, String time, String title, String type) {
            this.eventId = eventId;
            this.time = time;
            this.title = title;
            this.type = type;
        }

        // Getters and Setters
        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public ShiftInfo getShift() { return shift; }
    public void setShift(ShiftInfo shift) { this.shift = shift; }
    public List<TodayEvent> getEvents() { return events; }
    public void setEvents(List<TodayEvent> events) { this.events = events; }
    public String getWorkingStatus() { return workingStatus; }
    public void setWorkingStatus(String workingStatus) { this.workingStatus = workingStatus; }
}
