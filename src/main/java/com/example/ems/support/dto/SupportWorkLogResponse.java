package com.example.ems.support.dto;

public class SupportWorkLogResponse {

    private Long id;
    private Long engineerId;
    private String engineerName;
    private String startedAt;
    private String endedAt;
    private Double actualHours;
    private String description;
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
