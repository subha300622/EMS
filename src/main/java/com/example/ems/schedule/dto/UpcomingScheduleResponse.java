package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class UpcomingScheduleResponse {

    private List<UpcomingEvent> events;

    public UpcomingScheduleResponse() {}

    public UpcomingScheduleResponse(List<UpcomingEvent> events) {
        this.events = events;
    }

    public static class UpcomingEvent {
        @Schema(example = "1")
        private Long eventId;
        @Schema(example = "string")
        private String eventType;
        @Schema(example = "Project Deliverables")
        private String title;
        @Schema(example = "string")
        private String dateTime;
        @Schema(example = "ACTIVE")
        private String status;

        public UpcomingEvent() {}

        public UpcomingEvent(Long eventId, String eventType, String title, String dateTime, String status) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.title = title;
            this.dateTime = dateTime;
            this.status = status;
        }

        // Getters and Setters
        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public List<UpcomingEvent> getEvents() { return events; }
    public void setEvents(List<UpcomingEvent> events) { this.events = events; }
}
