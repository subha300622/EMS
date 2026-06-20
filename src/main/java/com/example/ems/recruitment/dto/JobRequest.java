package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Schema(example = "Project Deliverables")
    private String title;

    @NotBlank(message = "Department is required")
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
    private String status; // Optional, defaults to DRAFT

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
