package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.EmploymentType;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.entity.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobResponse {

    private Long id;
    private Long organizationId;
    private Long departmentId;
    private String department;
    private String title;
    private String slug;
    private String publicUrl;
    private String location;
    private EmploymentType employmentType;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer openings;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryRange;
    private String description;
    private List<String> requirements;
    private LocalDate applicationDeadline;
    private JobStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponse() {}

    public JobResponse(Job job) {
        this(job, "http://localhost:3000");
    }

    public JobResponse(Job job, String baseUrl) {
        this.id = job.getId();
        this.organizationId = job.getOrganizationId();
        this.departmentId = job.getDepartmentId();
        this.department = job.getDepartment();
        this.title = job.getTitle();
        this.slug = job.getSlug();
        String base = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "http://localhost:3000";
        this.publicUrl = base + "/jobs/" + (job.getSlug() != null ? job.getSlug() : "");
        this.location = job.getLocation();
        this.employmentType = job.getEmploymentType();
        this.experienceMin = job.getExperienceMin();
        this.experienceMax = job.getExperienceMax();
        this.openings = job.getOpenings();
        this.salaryMin = job.getSalaryMin();
        this.salaryMax = job.getSalaryMax();
        this.salaryRange = job.getSalaryRange();
        this.description = job.getDescription();
        if (job.getRequirements() != null && !job.getRequirements().isBlank()) {
            this.requirements = Arrays.asList(job.getRequirements().split("\\s*,\\s*|\\r?\\n"));
        } else {
            this.requirements = Collections.emptyList();
        }
        this.applicationDeadline = job.getApplicationDeadline();
        this.status = job.getStatus();
        this.publishedAt = job.getPublishedAt();
        this.createdAt = job.getCreatedAt();
        this.updatedAt = job.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public Integer getExperienceMin() { return experienceMin; }
    public void setExperienceMin(Integer experienceMin) { this.experienceMin = experienceMin; }

    public Integer getExperienceMax() { return experienceMax; }
    public void setExperienceMax(Integer experienceMax) { this.experienceMax = experienceMax; }

    public Integer getOpenings() { return openings; }
    public void setOpenings(Integer openings) { this.openings = openings; }

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
