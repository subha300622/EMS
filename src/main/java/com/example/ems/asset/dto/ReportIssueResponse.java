package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportIssueResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String ticketId;
    @Schema(example = "1")
    private Long assetId;
    @Schema(example = "string")
    private String assetName;
    @Schema(example = "string")
    private String issueType;
    @Schema(example = "string")
    private String severity;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime reportedAt;
    @Schema(example = "string")
    private String assignedTeam;
    @Schema(example = "2026-06-19")
    private LocalDate resolutionETA;

    public ReportIssueResponse() {}

    public ReportIssueResponse(Long id, String ticketId, Long assetId, String assetName, String issueType, String severity, String title, String description, String status, LocalDateTime reportedAt, String assignedTeam, LocalDate resolutionETA) {
        this.id = id;
        this.ticketId = ticketId;
        this.assetId = assetId;
        this.assetName = assetName;
        this.issueType = issueType;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.status = status;
        this.reportedAt = reportedAt;
        this.assignedTeam = assignedTeam;
        this.resolutionETA = resolutionETA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

    public LocalDate getResolutionETA() { return resolutionETA; }
    public void setResolutionETA(LocalDate resolutionETA) { this.resolutionETA = resolutionETA; }
}
