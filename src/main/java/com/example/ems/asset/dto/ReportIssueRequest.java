package com.example.ems.asset.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class ReportIssueRequest {

    @NotBlank(message = "Issue type is required")
    @Schema(example = "string")
    private String issueType; // HARDWARE, SOFTWARE, NETWORK

    @NotBlank(message = "Severity is required")
    @Schema(example = "string")
    private String severity; // LOW, MEDIUM, HIGH

    @NotBlank(message = "Title is required")
    @Schema(example = "Project Deliverables")
    private String title;

    @NotBlank(message = "Description is required")
    @Schema(example = "Detailed description of the item")
    private String description;

    public ReportIssueRequest() {}

    public ReportIssueRequest(String issueType, String severity, String title, String description) {
        this.issueType = issueType;
        this.severity = severity;
        this.title = title;
        this.description = description;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
