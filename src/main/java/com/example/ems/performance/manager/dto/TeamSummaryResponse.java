package com.example.ems.performance.manager.dto;

public class TeamSummaryResponse {
    private int reviewsCompleted;
    private int pendingReviews;
    private double avgTeamRating;
    private String teamBand;
    private int promotionEligibleCount;

    public int getReviewsCompleted() { return reviewsCompleted; }
    public void setReviewsCompleted(int reviewsCompleted) { this.reviewsCompleted = reviewsCompleted; }

    public int getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(int pendingReviews) { this.pendingReviews = pendingReviews; }

    public double getAvgTeamRating() { return avgTeamRating; }
    public void setAvgTeamRating(double avgTeamRating) { this.avgTeamRating = avgTeamRating; }

    public String getTeamBand() { return teamBand; }
    public void setTeamBand(String teamBand) { this.teamBand = teamBand; }

    public int getPromotionEligibleCount() { return promotionEligibleCount; }
    public void setPromotionEligibleCount(int promotionEligibleCount) { this.promotionEligibleCount = promotionEligibleCount; }
}
