package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.appraisal.entity.AppraisalCycle;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppraisalCycleResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String name;
    @Schema(example = "2026-06-19")
    private LocalDate startDate;
    @Schema(example = "2026-06-19")
    private LocalDate endDate;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public AppraisalCycleResponse() {}

    public AppraisalCycleResponse(AppraisalCycle cycle) {
        this.id = cycle.getId();
        this.name = cycle.getName();
        this.startDate = cycle.getStartDate();
        this.endDate = cycle.getEndDate();
        this.status = cycle.getStatus();
        this.createdAt = cycle.getCreatedAt();
        this.updatedAt = cycle.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
