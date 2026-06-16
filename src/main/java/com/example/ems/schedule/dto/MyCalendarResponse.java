package com.example.ems.schedule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class MyCalendarResponse {

    private List<CalendarEvent> events;

    public MyCalendarResponse() {}

    public MyCalendarResponse(List<CalendarEvent> events) {
        this.events = events;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CalendarEvent {
        private Long eventId;
        private String eventType; // MEETING, SHIFT, LEAVE, HOLIDAY
        private String title;
        private String startDateTime;
        private String endDateTime;
        private String startDate;
        private String endDate;
        private String location;
        private String status;

        // Getters and Setters
        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStartDateTime() { return startDateTime; }
        public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
        public String getEndDateTime() { return endDateTime; }
        public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public List<CalendarEvent> getEvents() { return events; }
    public void setEvents(List<CalendarEvent> events) { this.events = events; }
}
