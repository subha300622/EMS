package com.example.ems.appraisal.dto;

import java.time.LocalDateTime;

public class AppraisalConfigurationVersionResponseDto {

    private Long id;
    private Integer versionNumber;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private AppraisalConfigurationSnapshotDto snapshot;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public AppraisalConfigurationSnapshotDto getSnapshot() { return snapshot; }
    public void setSnapshot(AppraisalConfigurationSnapshotDto snapshot) { this.snapshot = snapshot; }
}
