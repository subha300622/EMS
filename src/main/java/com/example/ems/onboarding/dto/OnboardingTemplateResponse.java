package com.example.ems.onboarding.dto;

import java.time.LocalDate;
import java.util.List;

public class OnboardingTemplateResponse {

    private String id; // maps to templateCode (e.g. tpl-eng-001)
    private String templateCode;
    private String name;
    private String description;
    private int phases;
    private int tasks;
    private String departmentId;
    private String dept;
    private String deptColor;
    private String avgDays;
    private int usageCount;
    private String status;
    private int version;
    private String designation;
    private String employmentType;
    private String experienceLevel;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean isDefault;
    private List<OnboardingTemplateCreateRequest.SectionRequest> sections;
    private List<OnboardingTemplateCreateRequest.DocumentRequest> documents;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPhases() { return phases; }
    public void setPhases(int phases) { this.phases = phases; }

    public int getTasks() { return tasks; }
    public void setTasks(int tasks) { this.tasks = tasks; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getDeptColor() { return deptColor; }
    public void setDeptColor(String deptColor) { this.deptColor = deptColor; }

    public String getAvgDays() { return avgDays; }
    public void setAvgDays(String avgDays) { this.avgDays = avgDays; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public boolean getIsDefault() { return isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }

    public List<OnboardingTemplateCreateRequest.SectionRequest> getSections() { return sections; }
    public void setSections(List<OnboardingTemplateCreateRequest.SectionRequest> sections) { this.sections = sections; }

    public List<OnboardingTemplateCreateRequest.DocumentRequest> getDocuments() { return documents; }
    public void setDocuments(List<OnboardingTemplateCreateRequest.DocumentRequest> documents) { this.documents = documents; }
}
