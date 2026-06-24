package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.appraisal.entity.Appraisal;

import java.time.LocalDateTime;

public class AppraisalResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "john.doe@example.com")
    private String employeeEmail;
    @Schema(example = "Engineering")
    private String employeeDepartment;
    @Schema(example = "Software Engineer")
    private String employeeDesignation;
    @Schema(example = "1")
    private Long cycleId;
    @Schema(example = "string")
    private String cycleName;
    @Schema(example = "1.0")
    private Double selfRating;
    @Schema(example = "string")
    private String selfReview;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime selfReviewSubmittedAt;
    @Schema(example = "1")
    private Long reviewerId;
    @Schema(example = "string")
    private String reviewerName;
    @Schema(example = "1")
    private Double managerRating;
    @Schema(example = "string")
    private String managerReview;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime managerReviewSubmittedAt;
    @Schema(example = "1")
    private Double finalRating;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public AppraisalResponse() {}

    public AppraisalResponse(Appraisal app) {
        this.id = app.getId();
        this.selfRating = app.getSelfRating();
        this.selfReview = app.getSelfReview();
        this.selfReviewSubmittedAt = app.getSelfReviewSubmittedAt();
        this.managerRating = app.getManagerRating();
        this.managerReview = app.getManagerReview();
        this.managerReviewSubmittedAt = app.getManagerReviewSubmittedAt();
        this.finalRating = app.getFinalRating();
        this.status = app.getStatus() != null ? app.getStatus().name() : null;
        this.createdAt = app.getCreatedAt();
        this.updatedAt = app.getUpdatedAt();

        if (app.getEmployee() != null) {
            this.employeeId = app.getEmployee().getId();
            this.employeeName = app.getEmployee().getFullName();
            this.employeeEmail = app.getEmployee().getEmail();
            this.employeeDepartment = app.getEmployee().getDepartment();
            this.employeeDesignation = app.getEmployee().getDesignation();
        }

        if (app.getCycle() != null) {
            this.cycleId = app.getCycle().getId();
            this.cycleName = app.getCycle().getName();
        }

        if (app.getReviewer() != null) {
            this.reviewerId = app.getReviewer().getId();
            this.reviewerName = app.getReviewer().getFullName();
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

    public String getEmployeeDepartment() { return employeeDepartment; }
    public void setEmployeeDepartment(String employeeDepartment) { this.employeeDepartment = employeeDepartment; }

    public String getEmployeeDesignation() { return employeeDesignation; }
    public void setEmployeeDesignation(String employeeDesignation) { this.employeeDesignation = employeeDesignation; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public Double getSelfRating() { return selfRating; }
    public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }

    public String getSelfReview() { return selfReview; }
    public void setSelfReview(String selfReview) { this.selfReview = selfReview; }

    public LocalDateTime getSelfReviewSubmittedAt() { return selfReviewSubmittedAt; }
    public void setSelfReviewSubmittedAt(LocalDateTime selfReviewSubmittedAt) { this.selfReviewSubmittedAt = selfReviewSubmittedAt; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public Double getManagerRating() { return managerRating; }
    public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

    public String getManagerReview() { return managerReview; }
    public void setManagerReview(String managerReview) { this.managerReview = managerReview; }

    public LocalDateTime getManagerReviewSubmittedAt() { return managerReviewSubmittedAt; }
    public void setManagerReviewSubmittedAt(LocalDateTime managerReviewSubmittedAt) { this.managerReviewSubmittedAt = managerReviewSubmittedAt; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
