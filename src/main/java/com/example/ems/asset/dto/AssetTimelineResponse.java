package com.example.ems.asset.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AssetTimelineResponse {
    private List<TimelineEventItem> events;

    public AssetTimelineResponse() {}

    public AssetTimelineResponse(List<TimelineEventItem> events) {
        this.events = events;
    }

    public List<TimelineEventItem> getEvents() {
        return events;
    }

    public void setEvents(List<TimelineEventItem> events) {
        this.events = events;
    }

    public static class TimelineEventItem {
        private String event;
        private String performedBy;
        private LocalDateTime date;
        private String remarks;

        public TimelineEventItem() {}

        public TimelineEventItem(String event, String performedBy, LocalDateTime date, String remarks) {
            this.event = event;
            this.performedBy = performedBy;
            this.date = date;
            this.remarks = remarks;
        }

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}
