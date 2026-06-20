package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class TicketTimelineResponse {
    @Schema(example = "1")
    private Long ticketId;
    @Schema(example = "string")
    private String ticketNumber;
    private List<TimelineActivityDto> activities;

    public TicketTimelineResponse() {}

    public TicketTimelineResponse(Long ticketId, String ticketNumber, List<TimelineActivityDto> activities) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.activities = activities;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public List<TimelineActivityDto> getActivities() { return activities; }
    public void setActivities(List<TimelineActivityDto> activities) { this.activities = activities; }

    public static class TimelineActivityDto {
        @Schema(example = "1")
        private Long activityId;
        @Schema(example = "string")
        private String event;
        @Schema(example = "string")
        private String performedBy;
        @Schema(example = "string")
        private String timestamp;

        public TimelineActivityDto() {}

        public TimelineActivityDto(Long activityId, String event, String performedBy, String timestamp) {
            this.activityId = activityId;
            this.event = event;
            this.performedBy = performedBy;
            this.timestamp = timestamp;
        }

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
