package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Candidate;
import com.example.ems.recruitment.entity.TalentPoolStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TalentPoolCandidateResponse {

    private Long id;
    private Long organizationId;
    private String fullName;
    private String email;
    private String phone;
    private Double experienceYears;
    private String currentCompany;
    private String currentDesignation;
    private BigDecimal expectedSalary;
    private String resumeUrl;
    private TalentPoolStatus talentPoolStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TalentPoolCandidateResponse() {}

    public TalentPoolCandidateResponse(Candidate candidate) {
        this.id = candidate.getId();
        this.organizationId = candidate.getOrganizationId();
        this.fullName = candidate.getFullName();
        this.email = candidate.getEmail();
        this.phone = candidate.getPhone();
        this.experienceYears = candidate.getExperienceYears();
        this.currentCompany = candidate.getCurrentCompany();
        this.currentDesignation = candidate.getCurrentDesignation();
        this.expectedSalary = candidate.getExpectedSalary();
        this.resumeUrl = candidate.getResumeUrl();
        this.talentPoolStatus = candidate.getTalentPoolStatus();
        this.createdAt = candidate.getCreatedAt();
        this.updatedAt = candidate.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Double experienceYears) { this.experienceYears = experienceYears; }

    public String getCurrentCompany() { return currentCompany; }
    public void setCurrentCompany(String currentCompany) { this.currentCompany = currentCompany; }

    public String getCurrentDesignation() { return currentDesignation; }
    public void setCurrentDesignation(String currentDesignation) { this.currentDesignation = currentDesignation; }

    public BigDecimal getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(BigDecimal expectedSalary) { this.expectedSalary = expectedSalary; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public TalentPoolStatus getTalentPoolStatus() { return talentPoolStatus; }
    public void setTalentPoolStatus(TalentPoolStatus talentPoolStatus) { this.talentPoolStatus = talentPoolStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
