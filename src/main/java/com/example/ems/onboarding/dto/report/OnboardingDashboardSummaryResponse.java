package com.example.ems.onboarding.dto.report;

public class OnboardingDashboardSummaryResponse {

    private long total;
    private long preJoining;
    private long inProgress;
    private long pendingApproval;
    private long approved;
    private long completed;
    private long onHold;
    private long cancelled;
    private long overdue;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getPreJoining() { return preJoining; }
    public void setPreJoining(long preJoining) { this.preJoining = preJoining; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getPendingApproval() { return pendingApproval; }
    public void setPendingApproval(long pendingApproval) { this.pendingApproval = pendingApproval; }

    public long getApproved() { return approved; }
    public void setApproved(long approved) { this.approved = approved; }

    public long getCompleted() { return completed; }
    public void setCompleted(long completed) { this.completed = completed; }

    public long getOnHold() { return onHold; }
    public void setOnHold(long onHold) { this.onHold = onHold; }

    public long getCancelled() { return cancelled; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }

    public long getOverdue() { return overdue; }
    public void setOverdue(long overdue) { this.overdue = overdue; }
}
