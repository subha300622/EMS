package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.offboarding.entity.OffboardingTask;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OffboardingTaskResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19")
    private LocalDate dueDate;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime completedAt;

    public OffboardingTaskResponse() {}

    public OffboardingTaskResponse(OffboardingTask task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.dueDate = task.getDueDate();
        this.completedAt = task.getCompletedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
