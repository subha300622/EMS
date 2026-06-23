package com.example.ems.performance.manager.dto;

import java.time.LocalDateTime;

public class SaveManagerRatingResponse {
    private Long reviewId;
    private String status;
    private Double managerRating;
    private Double finalScore;
    private LocalDateTime updatedAt;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getManagerRating() { return managerRating; }
    public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
