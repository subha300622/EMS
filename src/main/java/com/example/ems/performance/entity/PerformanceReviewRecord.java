package com.example.ems.performance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_review_records", indexes = {
        @Index(name = "idx_perf_review_org_status", columnList = "organization_id, status"),
        @Index(name = "idx_perf_review_employee", columnList = "employee_id"),
        @Index(name = "idx_perf_review_cycle", columnList = "cycle_id")
})
public class PerformanceReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    @JsonIgnoreProperties({"organization"})
    private PerformanceReviewCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee reviewer;

    @Column(nullable = false, length = 50)
    private String status = "DRAFT";

    // Self Review Details
    @Column(name = "self_score", precision = 5, scale = 2)
    private BigDecimal selfScore;

    @Column(name = "self_feedback", columnDefinition = "TEXT")
    private String selfFeedback;

    @Column(name = "self_submitted_at")
    private LocalDateTime selfSubmittedAt;

    // Manager Review Details
    @Column(name = "manager_score", precision = 5, scale = 2)
    private BigDecimal managerScore;

    @Column(name = "manager_feedback", columnDefinition = "TEXT")
    private String managerFeedback;

    @Column(name = "manager_submitted_at")
    private LocalDateTime managerSubmittedAt;

    // Snapshotted Attendance Metrics
    @Column(name = "attendance_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal attendanceScore = BigDecimal.ZERO;

    @Column(name = "leaves_taken", nullable = false)
    private Integer leavesTaken = 0;

    @Column(name = "attendance_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal attendancePercentage = new BigDecimal("100.00");

    // Calculated Scores
    @Column(name = "kpi_weighted_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal kpiWeightedScore = BigDecimal.ZERO;

    @Column(name = "calculated_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal calculatedScore = BigDecimal.ZERO;

    @Column(name = "final_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal finalScore = BigDecimal.ZERO;

    @Column(name = "rating_band", length = 50)
    private String ratingBand;

    // Snapshot and Calculation Versioning
    @Column(name = "calculation_version", nullable = false)
    private Integer calculationVersion = 1;

    @Column(name = "formula_version", nullable = false, length = 20)
    private String formulaVersion = "v1.0";

    @Column(name = "current_calculation_run_id")
    private Long currentCalculationRunId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "jsonb")
    private String snapshotData;

    @Column(name = "snapshot_version", nullable = false)
    private Integer snapshotVersion = 1;

    @Column(name = "snapshot_hash", length = 64)
    private String snapshotHash;

    @Column(name = "snapshot_created_at")
    private LocalDateTime snapshotCreatedAt;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee calculatedBy;

    // Approval Integration
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee submittedBy;

    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee approvedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee rejectedBy;

    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reopened_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee reopenedBy;

    // Publishing and Locking
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee publishedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee lockedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PerformanceReviewRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public PerformanceReviewCycle getCycle() { return cycle; }
    public void setCycle(PerformanceReviewCycle cycle) { this.cycle = cycle; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }

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

    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }

    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }

    public String getSnapshotHash() { return snapshotHash; }
    public void setSnapshotHash(String snapshotHash) { this.snapshotHash = snapshotHash; }

    public LocalDateTime getSnapshotCreatedAt() { return snapshotCreatedAt; }
    public void setSnapshotCreatedAt(LocalDateTime snapshotCreatedAt) { this.snapshotCreatedAt = snapshotCreatedAt; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public Employee getCalculatedBy() { return calculatedBy; }
    public void setCalculatedBy(Employee calculatedBy) { this.calculatedBy = calculatedBy; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Employee getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Employee submittedBy) { this.submittedBy = submittedBy; }

    public Long getApprovalWorkflowId() { return approvalWorkflowId; }
    public void setApprovalWorkflowId(Long approvalWorkflowId) { this.approvalWorkflowId = approvalWorkflowId; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public Employee getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Employee approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public Employee getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(Employee rejectedBy) { this.rejectedBy = rejectedBy; }

    public LocalDateTime getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(LocalDateTime reopenedAt) { this.reopenedAt = reopenedAt; }

    public Employee getReopenedBy() { return reopenedBy; }
    public void setReopenedBy(Employee reopenedBy) { this.reopenedBy = reopenedBy; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Employee getPublishedBy() { return publishedBy; }
    public void setPublishedBy(Employee publishedBy) { this.publishedBy = publishedBy; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public Employee getLockedBy() { return lockedBy; }
    public void setLockedBy(Employee lockedBy) { this.lockedBy = lockedBy; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
