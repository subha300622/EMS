package com.example.ems.onboarding.dto;

import java.util.List;

public class OnboardingTemplateUpdateRequest {

    private String name;
    private String description;
    private String status;
    private Boolean isDefault;
    private List<OnboardingTemplateCreateRequest.SectionRequest> sections;
    private List<OnboardingTemplateCreateRequest.DocumentRequest> documents;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public List<OnboardingTemplateCreateRequest.SectionRequest> getSections() { return sections; }
    public void setSections(List<OnboardingTemplateCreateRequest.SectionRequest> sections) { this.sections = sections; }

    public List<OnboardingTemplateCreateRequest.DocumentRequest> getDocuments() { return documents; }
    public void setDocuments(List<OnboardingTemplateCreateRequest.DocumentRequest> documents) { this.documents = documents; }
}
