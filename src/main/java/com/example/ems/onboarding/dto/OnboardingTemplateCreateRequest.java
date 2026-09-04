package com.example.ems.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class OnboardingTemplateCreateRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String description;

    @NotBlank(message = "Department ID must not be blank")
    private String departmentId;

    @NotBlank(message = "Designation must not be blank")
    private String designation;

    @NotBlank(message = "Employment type must not be blank")
    private String employmentType;

    private String experienceLevel;

    @NotNull(message = "Effective from date must not be null")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private boolean isDefault;

    @NotEmpty(message = "Sections must contain at least one section")
    @Valid
    private List<SectionRequest> sections;

    private List<DocumentRequest> documents;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

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

    public List<SectionRequest> getSections() { return sections; }
    public void setSections(List<SectionRequest> sections) { this.sections = sections; }

    public List<DocumentRequest> getDocuments() { return documents; }
    public void setDocuments(List<DocumentRequest> documents) { this.documents = documents; }

    public static class SectionRequest {
        @NotBlank(message = "Section name must not be blank")
        private String name;

        @NotEmpty(message = "Section must contain at least one task")
        @Valid
        private List<TaskRequest> tasks;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<TaskRequest> getTasks() { return tasks; }
        public void setTasks(List<TaskRequest> tasks) { this.tasks = tasks; }
    }

    public static class TaskRequest {
        @NotBlank(message = "Task name must not be blank")
        private String name;

        private String ownerType;
        private String ownerId;
        private String verifiedBy;

        @Min(value = 0, message = "dueDays must be >= 0")
        private int dueDays;

        private String priority;
        private boolean mandatory;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOwnerType() { return ownerType; }
        public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

        public String getVerifiedBy() { return verifiedBy; }
        public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

        public int getDueDays() { return dueDays; }
        public void setDueDays(int dueDays) { this.dueDays = dueDays; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class DocumentRequest {
        @NotBlank(message = "Document name must not be blank")
        private String name;

        private boolean mandatory;

        @Min(value = 1, message = "maxSize must be greater than 0")
        private int maxSize;

        private List<String> allowedTypes;
        private boolean needVerification;
        private boolean visibleToEmployee;
        private boolean issuedByOrganization;
        private boolean autoVisibleToEmployee;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

        public List<String> getAllowedTypes() { return allowedTypes; }
        public void setAllowedTypes(List<String> allowedTypes) { this.allowedTypes = allowedTypes; }

        public boolean isNeedVerification() { return needVerification; }
        public void setNeedVerification(boolean needVerification) { this.needVerification = needVerification; }

        public boolean isVisibleToEmployee() { return visibleToEmployee; }
        public void setVisibleToEmployee(boolean visibleToEmployee) { this.visibleToEmployee = visibleToEmployee; }

        public boolean isIssuedByOrganization() { return issuedByOrganization; }
        public void setIssuedByOrganization(boolean issuedByOrganization) { this.issuedByOrganization = issuedByOrganization; }

        public boolean isAutoVisibleToEmployee() { return autoVisibleToEmployee; }
        public void setAutoVisibleToEmployee(boolean autoVisibleToEmployee) { this.autoVisibleToEmployee = autoVisibleToEmployee; }
    }
}
