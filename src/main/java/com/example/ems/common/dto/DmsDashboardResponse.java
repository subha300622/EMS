package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class DmsDashboardResponse {
    @Schema(example = "1")
    private long totalDocuments;
    @Schema(example = "1")
    private long pendingApprovals;
    @Schema(example = "1")
    private long approvedDocuments;
    @Schema(example = "1")
    private long rejectedDocuments;
    @Schema(example = "1")
    private long expiringSoon;
    @Schema(example = "1")
    private long totalShares;
    @Schema(example = "1")
    private long totalSignatureRequests;
    @Schema(example = "1")
    private long pendingSignatureRequests;

    public DmsDashboardResponse() {}

    public long getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long pendingApprovals) { this.pendingApprovals = pendingApprovals; }

    public long getApprovedDocuments() { return approvedDocuments; }
    public void setApprovedDocuments(long approvedDocuments) { this.approvedDocuments = approvedDocuments; }

    public long getRejectedDocuments() { return rejectedDocuments; }
    public void setRejectedDocuments(long rejectedDocuments) { this.rejectedDocuments = rejectedDocuments; }

    public long getExpiringSoon() { return expiringSoon; }
    public void setExpiringSoon(long expiringSoon) { this.expiringSoon = expiringSoon; }

    public long getTotalShares() { return totalShares; }
    public void setTotalShares(long totalShares) { this.totalShares = totalShares; }

    public long getTotalSignatureRequests() { return totalSignatureRequests; }
    public void setTotalSignatureRequests(long totalSignatureRequests) { this.totalSignatureRequests = totalSignatureRequests; }

    public long getPendingSignatureRequests() { return pendingSignatureRequests; }
    public void setPendingSignatureRequests(long pendingSignatureRequests) { this.pendingSignatureRequests = pendingSignatureRequests; }
}
