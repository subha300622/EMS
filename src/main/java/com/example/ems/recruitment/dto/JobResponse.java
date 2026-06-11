package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Job;

import java.time.LocalDateTime;

public class JobResponse {
    private Long id;
    private String title;
    private String department;
    private String location;
    private String description;
    private String requirements;
    private String salaryRange;
    private String status;
    private LocalDateTime createdAt;
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
