package com.example.ems.performance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Cryptographic Calculation Snapshot Details")
public class PerformanceSnapshotResponse {

    @Schema(description = "Review ID", example = "301")
    private Long reviewId;

    @Schema(description = "Snapshot iteration version", example = "1")
    private Integer snapshotVersion;

    @Schema(description = "SHA-256 hash of immutable snapshot payload", example = "a1b2c3d4e5f67890abcdef1234567890abcdef1234567890abcdef1234567890")
    private String snapshotHash;

    @Schema(description = "Snapshot creation timestamp", example = "2026-09-18T10:15:00")
    private LocalDateTime snapshotCreatedAt;

    @Schema(description = "Raw JSON payload of captured snapshot", example = "{\"employeeId\": 1025, \"selfScore\": 4.5, \"managerScore\": 4.0, \"kpiScores\": []}")
    private String snapshotJson;

    public PerformanceSnapshotResponse() {}

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Integer getSnapshotVersion() { return snapshotVersion; }
    public void setSnapshotVersion(Integer snapshotVersion) { this.snapshotVersion = snapshotVersion; }

    public String getSnapshotHash() { return snapshotHash; }
    public void setSnapshotHash(String snapshotHash) { this.snapshotHash = snapshotHash; }

    public LocalDateTime getSnapshotCreatedAt() { return snapshotCreatedAt; }
    public void setSnapshotCreatedAt(LocalDateTime snapshotCreatedAt) { this.snapshotCreatedAt = snapshotCreatedAt; }

    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
}
