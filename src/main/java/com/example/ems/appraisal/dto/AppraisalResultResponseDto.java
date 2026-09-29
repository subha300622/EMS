package com.example.ems.appraisal.dto;

import com.example.ems.appraisal.entity.AppraisalStatus;
import java.time.LocalDateTime;
import java.util.List;

public class AppraisalResultResponseDto {
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private Long cycleId;
    private String cycleName;
    private Long requestId;
    private Double finalRating;
    private String performanceCategory;
    private AppraisalStatus status;
    private SelfAssessmentDto selfAssessment;
    private List<ReviewStageDto> reviews;
    private LocalDateTime completedAt;
    private LocalDateTime publishedAt;

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public String getPerformanceCategory() { return performanceCategory; }
    public void setPerformanceCategory(String performanceCategory) { this.performanceCategory = performanceCategory; }

    public AppraisalStatus getStatus() { return status; }
    public void setStatus(AppraisalStatus status) { this.status = status; }

    public SelfAssessmentDto getSelfAssessment() { return selfAssessment; }
    public void setSelfAssessment(SelfAssessmentDto selfAssessment) { this.selfAssessment = selfAssessment; }

    public List<ReviewStageDto> getReviews() { return reviews; }
    public void setReviews(List<ReviewStageDto> reviews) { this.reviews = reviews; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
