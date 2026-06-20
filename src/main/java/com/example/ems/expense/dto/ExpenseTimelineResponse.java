package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class ExpenseTimelineResponse {
    @Schema(example = "1")
    private Long expenseId;
    private List<TimelineEventItem> timeline;

    public ExpenseTimelineResponse() {}

    public ExpenseTimelineResponse(Long expenseId, List<TimelineEventItem> timeline) {
        this.expenseId = expenseId;
        this.timeline = timeline;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public List<TimelineEventItem> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEventItem> timeline) {
        this.timeline = timeline;
    }

    public static class TimelineEventItem {
        @Schema(example = "string")
        private String event;
        @Schema(example = "string")
        private String performedBy;
        @Schema(example = "2026-06-19T10:00:00")
        private LocalDateTime date;

        public TimelineEventItem() {}

        public TimelineEventItem(String event, String performedBy, LocalDateTime date) {
            this.event = event;
            this.performedBy = performedBy;
            this.date = date;
        }

        public String getEvent() { return event; }
        public void setEvent(String event) { this.event = event; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }
}
