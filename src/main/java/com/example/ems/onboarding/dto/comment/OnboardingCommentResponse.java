package com.example.ems.onboarding.dto.comment;

import java.time.LocalDateTime;

public class OnboardingCommentResponse {

    private Long commentId;
    private Long onboardingId;
    private String comment;
    private CreatedUser createdBy;
    private LocalDateTime createdAt;

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public CreatedUser getCreatedBy() { return createdBy; }
    public void setCreatedBy(CreatedUser createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class CreatedUser {
        private String employeeId;
        private String fullName;

        public CreatedUser(String employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
        }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
