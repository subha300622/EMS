package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.EmploymentType;
import com.example.ems.recruitment.entity.Job;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PublicJobResponse {

    private Long jobId;
    private String title;
    private String slug;
    private String publicUrl;
    private String department;
    private String location;
    private EmploymentType employmentType;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer openings;
    private String description;
    private List<String> requirements;
    private LocalDate applicationDeadline;

    public PublicJobResponse() {}

    public PublicJobResponse(Job job) {
        this(job, null);
    }

    public PublicJobResponse(Job job, String baseUrl) {
        this.jobId = job.getId();
        this.title = job.getTitle();
        this.slug = job.getSlug();
        String base = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "";
        this.publicUrl = (!base.isBlank()) ? base + "/jobs/" + (job.getSlug() != null ? job.getSlug() : "") : "/jobs/" + (job.getSlug() != null ? job.getSlug() : "");
        this.department = job.getDepartment();
        this.location = job.getLocation();
        this.employmentType = job.getEmploymentType();
        this.experienceMin = job.getExperienceMin();
        this.experienceMax = job.getExperienceMax();
        this.openings = job.getOpenings();
        this.description = job.getDescription();
        if (job.getRequirements() != null && !job.getRequirements().isBlank()) {
            this.requirements = Arrays.asList(job.getRequirements().split("\\s*,\\s*|\\r?\\n"));
        } else {
            this.requirements = Collections.emptyList();
        }
        this.applicationDeadline = job.getApplicationDeadline();
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }
}
