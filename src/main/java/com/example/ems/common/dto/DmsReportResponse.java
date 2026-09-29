package com.example.ems.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Document management report response")
public class DmsReportResponse {

    @Schema(description = "Type of the report", example = "SUMMARY")
    private String reportType;

    @Schema(description = "Timestamp when report was generated", example = "2026-09-25T14:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "Total documents count", example = "120")
    private long totalDocumentsCount;

    @Schema(description = "Approved documents count", example = "90")
    private long approvedCount;

    @Schema(description = "Rejected documents count", example = "10")
    private long rejectedCount;

    @Schema(description = "Pending documents count", example = "20")
    private long pendingCount;

    @Schema(description = "Approval rate percentage", example = "75.0")
    private double approvalRate;

    @Schema(description = "Category breakdown count", example = "{\"POLICIES\": 40, \"CONTRACTS\": 80}")
    private Map<String, Long> categoryBreakdown;

    @Schema(description = "Total signature requests", example = "50")
    private long totalSignatureRequests;

    @Schema(description = "Signed requests count", example = "35")
    private long signedCount;

    @Schema(description = "Pending signature requests count", example = "10")
    private long pendingSignatureCount;

    @Schema(description = "Declined signature requests count", example = "5")
    private long declinedCount;

    public DmsReportResponse() {}

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public long getTotalDocumentsCount() { return totalDocumentsCount; }
    public void setTotalDocumentsCount(long totalDocumentsCount) { this.totalDocumentsCount = totalDocumentsCount; }
    public long getApprovedCount() { return approvedCount; }
    public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
    public long getPendingCount() { return pendingCount; }
    public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }
    public double getApprovalRate() { return approvalRate; }
    public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }
    public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public long getTotalSignatureRequests() { return totalSignatureRequests; }
    public void setTotalSignatureRequests(long totalSignatureRequests) { this.totalSignatureRequests = totalSignatureRequests; }
    public long getSignedCount() { return signedCount; }
    public void setSignedCount(long signedCount) { this.signedCount = signedCount; }
    public long getPendingSignatureCount() { return pendingSignatureCount; }
    public void setPendingSignatureCount(long pendingSignatureCount) { this.pendingSignatureCount = pendingSignatureCount; }
    public long getDeclinedCount() { return declinedCount; }
    public void setDeclinedCount(long declinedCount) { this.declinedCount = declinedCount; }
}
