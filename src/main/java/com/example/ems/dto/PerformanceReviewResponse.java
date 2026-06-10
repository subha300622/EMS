package com.example.ems.dto;

import com.example.ems.entity.*;
import java.time.LocalDateTime;

public class PerformanceReviewResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long cycleId;
    private String cycleName;
    private String reviewType;
    private Long reviewerId;
    private String reviewerName;
    private String achievements;
    private String areasForImprovement;
    private String comments;
    private Integer rating;
    private String status;
    private LocalDateTime createdAt;
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
