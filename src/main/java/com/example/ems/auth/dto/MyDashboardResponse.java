package com.example.ems.auth.dto;

public class MyDashboardResponse {
    private long pendingLeaves;
    private long pendingExpenses;
    private long assignedAssets;
    private long pendingReviews;
    private long openTickets;

    public MyDashboardResponse() {}

    public MyDashboardResponse(long pendingLeaves, long pendingExpenses, long assignedAssets, long pendingReviews, long openTickets) {
        this.pendingLeaves = pendingLeaves;
        this.pendingExpenses = pendingExpenses;
        this.assignedAssets = assignedAssets;
        this.pendingReviews = pendingReviews;
        this.openTickets = openTickets;
    }

    public long getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(long pendingLeaves) { this.pendingLeaves = pendingLeaves; }

    public long getPendingExpenses() { return pendingExpenses; }
    public void setPendingExpenses(long pendingExpenses) { this.pendingExpenses = pendingExpenses; }

    public long getAssignedAssets() { return assignedAssets; }
    public void setAssignedAssets(long assignedAssets) { this.assignedAssets = assignedAssets; }

    public long getPendingReviews() { return pendingReviews; }
    public void setPendingReviews(long pendingReviews) { this.pendingReviews = pendingReviews; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }
}
