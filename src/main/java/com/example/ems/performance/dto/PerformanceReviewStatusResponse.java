package com.example.ems.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Performance Review Status and Next Permissible Actions")
public class PerformanceReviewStatusResponse {

    @Schema(description = "Review record ID", example = "301")
    private Long reviewId;

    @Schema(description = "Performance review cycle ID", example = "10")
    private Long cycleId;

    @Schema(description = "Target employee ID", example = "1025")
    private Long employeeId;

    @Schema(description = "Current review lifecycle status", example = "CALCULATED")
    private String status;

    @Schema(description = "Current computed or final score", example = "4.25")
    private BigDecimal finalScore;

    @Schema(description = "Performance rating band", example = "EXCEEDS_EXPECTATIONS")
    private String ratingBand;

    @Schema(description = "Score calculation iteration version", example = "1")
    private Integer calculationVersion;

    @Schema(description = "Cryptographic snapshot version", example = "1")
    private Integer snapshotVersion;

    @Schema(description = "SHA-256 integrity hash of calculation snapshot", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String snapshotHash;

    @Schema(description = "Submission timestamp", example = "2026-09-18T10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "Approval timestamp", example = "2026-09-18T12:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Publication timestamp", example = "2026-09-18T14:00:00")
    private LocalDateTime publishedAt;

    @Schema(description = "Lock timestamp", example = "2026-09-18T15:00:00")
    private LocalDateTime lockedAt;

    @Schema(description = "Allowed next workflow actions for caller", example = "[\"RECALCULATE\", \"SUBMIT\", \"CANCEL\"]")
    private List<String> allowedActions;

    public PerformanceReviewStatusResponse() {}

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getRatingBand() { return ratingBand; }
    public void setRatingBand(String ratingBand) { this.ratingBand = ratingBand; }

    public Integer getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(Integer calculationVersion) { this.calculationVersion = calculationVersion; }

    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }

    public String getSnapshotHash() { return snapshotHash; }
    public void setSnapshotHash(String snapshotHash) { this.snapshotHash = snapshotHash; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public List<String> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }
}
