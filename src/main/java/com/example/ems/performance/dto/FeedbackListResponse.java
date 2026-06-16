package com.example.ems.performance.dto;

import java.util.List;

public class FeedbackListResponse {
    private List<FeedbackItem> feedback;

    public static class FeedbackItem {
        private Long id;
        private String feedbackType;
        private Integer rating;
        private String comments;
        private String receivedDate;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFeedbackType() { return feedbackType; }
        public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public String getReceivedDate() { return receivedDate; }
        public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }
    }

    public List<FeedbackItem> getFeedback() { return feedback; }
    public void setFeedback(List<FeedbackItem> feedback) { this.feedback = feedback; }
}
