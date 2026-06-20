package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.recruitment.entity.Job;

import java.time.LocalDateTime;

public class JobResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Project Deliverables")
    private String title;
    @Schema(example = "Engineering")
    private String department;
    @Schema(example = "Bangalore")
    private String location;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "string")
    private String requirements;
    @Schema(example = "string")
    private String salaryRange;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public JobResponse() {}

    public JobResponse(Job job) {
        this.id = job.getId();
        this.title = job.getTitle();
        this.department = job.getDepartment();
        this.location = job.getLocation();
        this.description = job.getDescription();
        this.requirements = job.getRequirements();
        this.salaryRange = job.getSalaryRange();
        this.status = job.getStatus();
        this.createdAt = job.getCreatedAt();
        this.updatedAt = job.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
