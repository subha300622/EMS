package com.example.ems.performance.dto;

import java.util.List;

public class ReviewCyclesResponse {
    private List<CycleSummary> cycles;

    public static class CycleSummary {
        private Long reviewId;
        private String cycleName;
        private String period;
        private String status;
        private String dueDate;
        private Integer completionPercentage;

        // Getters and Setters
        public Long getReviewId() { return reviewId; }
        public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
        public String getCycleName() { return cycleName; }
        public void setCycleName(String cycleName) { this.cycleName = cycleName; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public Integer getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }
    }

    public List<CycleSummary> getCycles() { return cycles; }
    public void setCycles(List<CycleSummary> cycles) { this.cycles = cycles; }
}
