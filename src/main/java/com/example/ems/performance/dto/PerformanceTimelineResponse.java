package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class PerformanceTimelineResponse {
    private List<TimelineItem> timeline;

    public static class TimelineItem {
        @Schema(example = "string")
        private String event;
        @Schema(example = "string")
        private String performedBy;
        @Schema(example = "string")
        private String date;
        @Schema(example = "Detailed description of the item")
        private String description;

        // Getters and Setters
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }
        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public List<TimelineItem> getTimeline() { return timeline; }
    public void setTimeline(List<TimelineItem> timeline) { this.timeline = timeline; }
}
