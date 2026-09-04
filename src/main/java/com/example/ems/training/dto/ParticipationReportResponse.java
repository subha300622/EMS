package com.example.ems.training.dto;

public class ParticipationReportResponse {
    private String groupName; // E.g., Department name, Team name
    private long totalAssigned;
    private long totalAccepted;
    private long totalDeclined;
    private double responseRate;

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public long getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(long totalAssigned) { this.totalAssigned = totalAssigned; }

    public long getTotalAccepted() { return totalAccepted; }
    public void setTotalAccepted(long totalAccepted) { this.totalAccepted = totalAccepted; }

    public long getTotalDeclined() { return totalDeclined; }
    public void setTotalDeclined(long totalDeclined) { this.totalDeclined = totalDeclined; }

    public double getResponseRate() { return responseRate; }
    public void setResponseRate(double responseRate) { this.responseRate = responseRate; }
}
