package com.example.ems.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support Ticket Work Log Response")
public class SupportWorkLogResponse {

    @Schema(description = "Work log ID", example = "10")
    private Long id;

    @Schema(description = "Engineer ID", example = "42")
    private Long engineerId;

    @Schema(description = "Engineer Name", example = "Jane Doe")
    private String engineerName;

    @Schema(description = "Work session start timestamp", example = "2026-09-25T10:00:00Z")
    private String startedAt;

    @Schema(description = "Work session end timestamp", example = "2026-09-25T11:30:00Z")
    private String endedAt;

    @Schema(description = "Actual hours logged", example = "1.5")
    private Double actualHours;

    @Schema(description = "Work session description", example = "Analyzed network route diagnostics.")
    private String description;

    @Schema(description = "Log creation timestamp", example = "2026-09-25T11:30:00Z")
    private String createdAt;

    public SupportWorkLogResponse() {}

    public SupportWorkLogResponse(Long id, Long engineerId, String engineerName, String startedAt, String endedAt, Double actualHours, String description, String createdAt) {
        this.id = id;
        this.engineerId = engineerId;
        this.engineerName = engineerName;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.actualHours = actualHours;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEngineerId() { return engineerId; }
    public void setEngineerId(Long engineerId) { this.engineerId = engineerId; }

    public String getEngineerName() { return engineerName; }
    public void setEngineerName(String engineerName) { this.engineerName = engineerName; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
