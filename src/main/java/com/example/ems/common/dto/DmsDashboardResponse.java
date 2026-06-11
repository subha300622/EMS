package com.example.ems.common.dto;

public class DmsDashboardResponse {
    private long totalDocuments;
    private long pendingApprovals;
    private long approvedDocuments;
    private long rejectedDocuments;
    private long expiringSoon;
    private long totalShares;
    private long totalSignatureRequests;
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
