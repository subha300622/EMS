package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public class PayrollTimelineResponse {

    private List<TimelineEventItem> events;

    public PayrollTimelineResponse() {}

    public PayrollTimelineResponse(List<TimelineEventItem> events) {
        this.events = events;
    }

    public List<TimelineEventItem> getEvents() { return events; }
    public void setEvents(List<TimelineEventItem> events) { this.events = events; }

    public static class TimelineEventItem {
        @Schema(example = "2026-06-19")
        private LocalDate date;
        @Schema(example = "ACTIVE")
        private String status;
        @Schema(example = "string")
        private String performedBy;

        public TimelineEventItem() {}

        public TimelineEventItem(LocalDate date, String status, String performedBy) {
            this.date = date;
            this.status = status;
            this.performedBy = performedBy;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    }
}
