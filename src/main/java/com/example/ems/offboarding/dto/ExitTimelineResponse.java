package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class ExitTimelineResponse {

    private List<TimelineEventItem> events;

    public ExitTimelineResponse() {}

    public ExitTimelineResponse(List<TimelineEventItem> events) {
        this.events = events;
    }

    public List<TimelineEventItem> getEvents() { return events; }
    public void setEvents(List<TimelineEventItem> events) { this.events = events; }

    public static class TimelineEventItem {
        @Schema(example = "2026-06-19T10:00:00")
        private LocalDateTime date;
        @Schema(example = "string")
        private String action;
        @Schema(example = "string")
        private String performedBy;

        public TimelineEventItem() {}

        public TimelineEventItem(LocalDateTime date, String action, String performedBy) {
            this.date = date;
            this.action = action;
            this.performedBy = performedBy;
        }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    }
}
