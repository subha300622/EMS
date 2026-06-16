package com.example.ems.schedule.dto;

import java.util.List;

public class ScheduleTimelineResponse {

    private List<TimelineActivity> activities;

    public ScheduleTimelineResponse() {}

    public ScheduleTimelineResponse(List<TimelineActivity> activities) {
        this.activities = activities;
    }

    public static class TimelineActivity {
        private String event;
        private String performedBy;
        private String performedAt;
        private String description;

        public TimelineActivity() {}

        public TimelineActivity(String event, String performedBy, String performedAt, String description) {
            this.event = event;
            this.performedBy = performedBy;
            this.performedAt = performedAt;
            this.description = description;
        }

        // Getters and Setters
        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public String getPerformedAt() { return performedAt; }
        public void setPerformedAt(String performedAt) { this.performedAt = performedAt; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public List<TimelineActivity> getActivities() { return activities; }
    public void setActivities(List<TimelineActivity> activities) { this.activities = activities; }
}
