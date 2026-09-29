package com.example.ems.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Enterprise Performance Review Details Response")
public class EnterprisePerformanceReviewResponse {

    @Schema(description = "Review record ID", example = "301")
    private Long id;

    @Schema(description = "Review cycle ID", example = "10")
    private Long cycleId;

    @Schema(description = "Review cycle code", example = "CYC-2026-Q3")
    private String cycleCode;

    @Schema(description = "Review cycle name", example = "Q3 2026 Annual Performance Cycle")
    private String cycleName;

    @Schema(description = "Evaluated employee ID", example = "1025")
    private Long employeeId;

    @Schema(description = "Evaluated employee full name", example = "Sarah Jenkins")
    private String employeeName;

    @Schema(description = "Evaluated employee email", example = "sarah.jenkins@company.com")
    private String employeeEmail;

    @Schema(description = "Assigned primary reviewer ID", example = "1002")
    private Long reviewerId;

    @Schema(description = "Assigned primary reviewer name", example = "Alex Morgan")
    private String reviewerName;

    @Schema(description = "Current review workflow status", example = "PUBLISHED")
    private String status;

    @Schema(description = "Employee self-evaluation rating (1-5)", example = "4.20")
    private BigDecimal selfScore;

    @Schema(description = "Employee self-review qualitative feedback", example = "Delivered all assigned deliverables ahead of milestones.")
    private String selfFeedback;

    @Schema(description = "Self-review submission timestamp", example = "2026-09-10T10:15:00")
    private LocalDateTime selfSubmittedAt;

    @Schema(description = "Manager evaluation rating (1-5)", example = "4.30")
    private BigDecimal managerScore;

    @Schema(description = "Manager evaluation qualitative feedback", example = "Consistent high performer who effectively unblocked the team.")
    private String managerFeedback;

    @Schema(description = "Manager review submission timestamp", example = "2026-09-12T14:30:00")
    private LocalDateTime managerSubmittedAt;

    @Schema(description = "Attendance score (1-5)", example = "4.80")
    private BigDecimal attendanceScore;

    @Schema(description = "Total leaves taken during review period", example = "2")
    private Integer leavesTaken;

    @Schema(description = "Overall attendance percentage", example = "97.50")
    private BigDecimal attendancePercentage;

    @Schema(description = "Weighted KPI composite score", example = "4.15")
    private BigDecimal kpiWeightedScore;

    @Schema(description = "Overall automated calculated score", example = "4.25")
    private BigDecimal calculatedScore;

    @Schema(description = "Final adjudicated score", example = "4.25")
    private BigDecimal finalScore;

    @Schema(description = "Performance rating band", example = "EXCEEDS_EXPECTATIONS")
    private String ratingBand;

    @Schema(description = "Score calculation iteration version", example = "1")
    private Integer calculationVersion;

    @Schema(description = "Active calculation formula version code", example = "ENTERPRISE_V1")
    private String formulaVersion;

    @Schema(description = "ID of the calculation run", example = "501")
    private Long currentCalculationRunId;

    @Schema(description = "Snapshot iteration version", example = "1")
    private Integer snapshotVersion;

    @Schema(description = "SHA-256 integrity hash of calculation snapshot", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String snapshotHash;

    @Schema(description = "Timestamp when cryptographic snapshot was generated", example = "2026-09-13T09:00:00")
    private LocalDateTime snapshotCreatedAt;

    @Schema(description = "Calculation execution timestamp", example = "2026-09-13T09:05:00")
    private LocalDateTime calculatedAt;

    @Schema(description = "Name of user who triggered score calculation", example = "Alex Morgan")
    private String calculatedByName;

    @Schema(description = "Review submission timestamp for approval", example = "2026-09-13T11:00:00")
    private LocalDateTime submittedAt;

    @Schema(description = "Name of user who submitted review", example = "Alex Morgan")
    private String submittedByName;

    @Schema(description = "Approval workflow instance ID", example = "701")
    private Long approvalWorkflowId;

    @Schema(description = "Review approval timestamp", example = "2026-09-14T15:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Name of approver", example = "Jennifer Davis")
    private String approvedByName;

    @Schema(description = "Rejection commentary if review was rejected", example = "")
    private String rejectionReason;

    @Schema(description = "Rejection timestamp", example = "2026-09-14T16:00:00")
    private LocalDateTime rejectedAt;

    @Schema(description = "Name of reviewer who rejected", example = "")
    private String rejectedByName;

    @Schema(description = "Timestamp when review was reopened for correction", example = "2026-09-15T09:00:00")
    private LocalDateTime reopenedAt;

    @Schema(description = "Name of user who reopened review", example = "")
    private String reopenedByName;

    @Schema(description = "Review publication timestamp", example = "2026-09-16T10:00:00")
    private LocalDateTime publishedAt;

    @Schema(description = "Name of user who published review to employee", example = "Jennifer Davis")
    private String publishedByName;

    @Schema(description = "Terminal lock timestamp", example = "2026-09-18T10:00:00")
    private LocalDateTime lockedAt;

    @Schema(description = "Name of user who locked review record", example = "Jennifer Davis")
    private String lockedByName;

    @Schema(description = "Optimistic locking version", example = "1")
    private Long version;

    @Schema(description = "Creation timestamp", example = "2026-09-01T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-09-18T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Itemized KPI scores breakdown")
    private List<KpiScoreResponseDto> kpiScores = new ArrayList<>();

    public EnterprisePerformanceReviewResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleCode() { return cycleCode; }
    public void setCycleCode(String cycleCode) { this.cycleCode = cycleCode; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getSelfScore() { return selfScore; }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }

    public String getSelfFeedback() { return selfFeedback; }
    public void setSelfFeedback(String selfFeedback) { this.selfFeedback = selfFeedback; }

    public LocalDateTime getSelfSubmittedAt() { return selfSubmittedAt; }
    public void setSelfSubmittedAt(LocalDateTime selfSubmittedAt) { this.selfSubmittedAt = selfSubmittedAt; }

    public BigDecimal getManagerScore() { return managerScore; }
    public void setManagerScore(BigDecimal managerScore) { this.managerScore = managerScore; }

    public String getManagerFeedback() { return managerFeedback; }
    public void setManagerFeedback(String managerFeedback) { this.managerFeedback = managerFeedback; }

    public LocalDateTime getManagerSubmittedAt() { return managerSubmittedAt; }
    public void setManagerSubmittedAt(LocalDateTime managerSubmittedAt) { this.managerSubmittedAt = managerSubmittedAt; }

    public BigDecimal getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(BigDecimal attendanceScore) { this.attendanceScore = attendanceScore; }

    public Integer getLeavesTaken() { return leavesTaken; }
    public void setLeavesTaken(Integer leavesTaken) { this.leavesTaken = leavesTaken; }

    public BigDecimal getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(BigDecimal attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public BigDecimal getKpiWeightedScore() { return kpiWeightedScore; }
    public void setKpiWeightedScore(BigDecimal kpiWeightedScore) { this.kpiWeightedScore = kpiWeightedScore; }

    public BigDecimal getCalculatedScore() { return calculatedScore; }
    public void setCalculatedScore(BigDecimal calculatedScore) { this.calculatedScore = calculatedScore; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getRatingBand() { return ratingBand; }
    public void setRatingBand(String ratingBand) { this.ratingBand = ratingBand; }

    public Integer getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(Integer calculationVersion) { this.calculationVersion = calculationVersion; }

    public String getFormulaVersion() { return formulaVersion; }
    public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }

    public Long getCurrentCalculationRunId() { return currentCalculationRunId; }
    public void setCurrentCalculationRunId(Long currentCalculationRunId) { this.currentCalculationRunId = currentCalculationRunId; }

    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }

    public String getSnapshotHash() { return snapshotHash; }
    public void setSnapshotHash(String snapshotHash) { this.snapshotHash = snapshotHash; }

    public LocalDateTime getSnapshotCreatedAt() { return snapshotCreatedAt; }
    public void setSnapshotCreatedAt(LocalDateTime snapshotCreatedAt) { this.snapshotCreatedAt = snapshotCreatedAt; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public String getCalculatedByName() { return calculatedByName; }
    public void setCalculatedByName(String calculatedByName) { this.calculatedByName = calculatedByName; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getSubmittedByName() { return submittedByName; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }

    public Long getApprovalWorkflowId() { return approvalWorkflowId; }
    public void setApprovalWorkflowId(Long approvalWorkflowId) { this.approvalWorkflowId = approvalWorkflowId; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectedByName() { return rejectedByName; }
    public void setRejectedByName(String rejectedByName) { this.rejectedByName = rejectedByName; }

    public LocalDateTime getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(LocalDateTime reopenedAt) { this.reopenedAt = reopenedAt; }

    public String getReopenedByName() { return reopenedByName; }
    public void setReopenedByName(String reopenedByName) { this.reopenedByName = reopenedByName; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public String getPublishedByName() { return publishedByName; }
    public void setPublishedByName(String publishedByName) { this.publishedByName = publishedByName; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public String getLockedByName() { return lockedByName; }
    public void setLockedByName(String lockedByName) { this.lockedByName = lockedByName; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<KpiScoreResponseDto> getKpiScores() { return kpiScores; }
    public void setKpiScores(List<KpiScoreResponseDto> kpiScores) { this.kpiScores = kpiScores; }

    @Schema(description = "Individual KPI Evaluation Score Item")
    public static class KpiScoreResponseDto {
        @Schema(description = "KPI definition ID", example = "51")
        private Long kpiId;

        @Schema(description = "Unique KPI code", example = "KPI-SPRINT-VELOCITY")
        private String kpiCode;

        @Schema(description = "KPI display name", example = "Sprint Velocity & Delivery")
        private String kpiName;

        @Schema(description = "Measurement unit type", example = "PERCENTAGE")
        private String measurementType;

        @Schema(description = "Weight percentage in review (0-100)", example = "25.00")
        private BigDecimal weight;

        @Schema(description = "Target benchmark value", example = "100.00")
        private BigDecimal targetValue;

        @Schema(description = "Achieved actual value", example = "95.00")
        private BigDecimal actualValue;

        @Schema(description = "Normalized score on scale of 1-5", example = "4.50")
        private BigDecimal normalizedScore;

        @Schema(description = "Reviewer or employee commentary", example = "Exceeded sprint goal by delivering all key stretch features.")
        private String comments;

        public KpiScoreResponseDto() {}

        public Long getKpiId() { return kpiId; }
        public void setKpiId(Long kpiId) { this.kpiId = kpiId; }
        public String getKpiCode() { return kpiCode; }
        public void setKpiCode(String kpiCode) { this.kpiCode = kpiCode; }
        public String getKpiName() { return kpiName; }
        public void setKpiName(String kpiName) { this.kpiName = kpiName; }
        public String getMeasurementType() { return measurementType; }
        public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getTargetValue() { return targetValue; }
        public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }
        public BigDecimal getActualValue() { return actualValue; }
        public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
        public BigDecimal getNormalizedScore() { return normalizedScore; }
        public void setNormalizedScore(BigDecimal normalizedScore) { this.normalizedScore = normalizedScore; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}
