package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.EmploymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    private Long departmentId;
    private String department;
    private String location;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @Min(value = 0, message = "Experience min cannot be negative")
    private Integer experienceMin;

    @Min(value = 0, message = "Experience max cannot be negative")
    private Integer experienceMax;

    @Min(value = 1, message = "Openings must be at least 1")
    private Integer openings;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String description;
    private List<String> requirements;
    private LocalDate applicationDeadline;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

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

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }
}
