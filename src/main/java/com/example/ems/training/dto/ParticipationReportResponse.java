package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training Participation Report Response")
public class ParticipationReportResponse {
    @Schema(description = "Group name (Department or Team)", example = "Engineering")
    private String groupName; // E.g., Department name, Team name
    @Schema(description = "Total number of participants assigned", example = "25")
    private long totalAssigned;
    @Schema(description = "Total number of accepted responses", example = "22")
    private long totalAccepted;
    @Schema(description = "Total number of declined responses", example = "3")
    private long totalDeclined;
    @Schema(description = "Participation response rate percentage", example = "88.00")
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
