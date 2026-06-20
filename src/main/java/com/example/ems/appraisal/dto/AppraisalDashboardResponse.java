package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AppraisalDashboardResponse {
    @Schema(example = "1")
    private long totalAppraisals;
    @Schema(example = "1")
    private long pendingSelfReviews;
    @Schema(example = "1")
    private long pendingManagerReviews;
    @Schema(example = "1")
    private long finalizedAppraisals;
    @Schema(example = "100.00")
    private double averageRating;

    @Schema(example = "1")
    private long totalIncrements;
    @Schema(example = "1")
    private long pendingIncrements;
    @Schema(example = "1")
    private long approvedIncrements;
    @Schema(example = "1")
    private long appliedIncrements;
    @Schema(example = "100.00")
    private double averageIncrementPercentage;

    public long getTotalAppraisals() { return totalAppraisals; }
    public void setTotalAppraisals(long totalAppraisals) { this.totalAppraisals = totalAppraisals; }

    public long getPendingSelfReviews() { return pendingSelfReviews; }
    public void setPendingSelfReviews(long pendingSelfReviews) { this.pendingSelfReviews = pendingSelfReviews; }

    public long getPendingManagerReviews() { return pendingManagerReviews; }
    public void setPendingManagerReviews(long pendingManagerReviews) { this.pendingManagerReviews = pendingManagerReviews; }

    public long getFinalizedAppraisals() { return finalizedAppraisals; }
    public void setFinalizedAppraisals(long finalizedAppraisals) { this.finalizedAppraisals = finalizedAppraisals; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public long getTotalIncrements() { return totalIncrements; }
    public void setTotalIncrements(long totalIncrements) { this.totalIncrements = totalIncrements; }

    public long getPendingIncrements() { return pendingIncrements; }
    public void setPendingIncrements(long pendingIncrements) { this.pendingIncrements = pendingIncrements; }

    public long getApprovedIncrements() { return approvedIncrements; }
    public void setApprovedIncrements(long approvedIncrements) { this.approvedIncrements = approvedIncrements; }

    public long getAppliedIncrements() { return appliedIncrements; }
    public void setAppliedIncrements(long appliedIncrements) { this.appliedIncrements = appliedIncrements; }

    public double getAverageIncrementPercentage() { return averageIncrementPercentage; }
    public void setAverageIncrementPercentage(double averageIncrementPercentage) { this.averageIncrementPercentage = averageIncrementPercentage; }
}
