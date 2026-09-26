package com.example.ems.onboarding.dto;

public class OnboardingStatsResponse {

    private long activeCount;
    private long preJoiningCount;
    private long completedCount;
    private long overdueTasks;
    private long pendingDocuments;
    private long uploadedDocuments;
    private long joiningThisWeek;

    public long getActiveCount() { return activeCount; }
    public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

    public long getPreJoiningCount() { return preJoiningCount; }
    public void setPreJoiningCount(long preJoiningCount) { this.preJoiningCount = preJoiningCount; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }

    public long getPendingDocuments() { return pendingDocuments; }
    public void setPendingDocuments(long pendingDocuments) { this.pendingDocuments = pendingDocuments; }

    public long getUploadedDocuments() { return uploadedDocuments; }
    public void setUploadedDocuments(long uploadedDocuments) { this.uploadedDocuments = uploadedDocuments; }

    public long getJoiningThisWeek() { return joiningThisWeek; }
    public void setJoiningThisWeek(long joiningThisWeek) { this.joiningThisWeek = joiningThisWeek; }
}
