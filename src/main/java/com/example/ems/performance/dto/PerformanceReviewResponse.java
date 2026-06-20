package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.performance.entity.PerformanceReview;

import java.time.LocalDateTime;

public class PerformanceReviewResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "john.doe@example.com")
    private String employeeEmail;
    @Schema(example = "1")
    private Long cycleId;
    @Schema(example = "string")
    private String cycleName;
    @Schema(example = "string")
    private String reviewType;
    @Schema(example = "1")
    private Long reviewerId;
    @Schema(example = "string")
    private String reviewerName;
    @Schema(example = "string")
    private String achievements;
    @Schema(example = "string")
    private String areasForImprovement;
    @Schema(example = "Excellent progress")
    private String comments;
    @Schema(example = "1")
    private Integer rating;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public PerformanceReviewResponse() {}

    public PerformanceReviewResponse(PerformanceReview r) {
        this.id = r.getId();
        this.reviewType = r.getReviewType();
        this.achievements = r.getAchievements();
        this.areasForImprovement = r.getAreasForImprovement();
        this.comments = r.getComments();
        this.rating = r.getRating();
        this.status = r.getStatus();
        this.createdAt = r.getCreatedAt();
        this.updatedAt = r.getUpdatedAt();
        if (r.getEmployee() != null) {
            this.employeeId = r.getEmployee().getId();
            this.employeeName = r.getEmployee().getFullName();
            this.employeeEmail = r.getEmployee().getEmail();
        }
        if (r.getCycle() != null) {
            this.cycleId = r.getCycle().getId();
            this.cycleName = r.getCycle().getName();
        }
        if (r.getReviewer() != null) {
            this.reviewerId = r.getReviewer().getId();
            this.reviewerName = r.getReviewer().getFullName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }
    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }
    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }
    public String getReviewType() { return reviewType; }
    public void setReviewType(String reviewType) { this.reviewType = reviewType; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }
    public String getAreasForImprovement() { return areasForImprovement; }
    public void setAreasForImprovement(String areasForImprovement) { this.areasForImprovement = areasForImprovement; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
