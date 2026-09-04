package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Application;
import com.example.ems.recruitment.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private String applicationNumber;
    private Long organizationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Double experienceYears;
    private String currentCompany;
    private String currentDesignation;
    private BigDecimal expectedSalary;
    private Integer noticePeriodDays;
    private String resumeUrl;
    private Long jobId;
    private String jobTitle;
    private String department;
    private ApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse() {}

    public ApplicationResponse(Application application) {
        this.id = application.getId();
        this.applicationNumber = application.getApplicationNumber();
        this.organizationId = application.getOrganizationId();
        if (application.getCandidate() != null) {
            this.candidateId = application.getCandidate().getId();
            this.candidateName = application.getCandidate().getFullName();
            this.candidateEmail = application.getCandidate().getEmail();
            this.candidatePhone = application.getCandidate().getPhone();
            this.experienceYears = application.getCandidate().getExperienceYears();
            this.currentCompany = application.getCandidate().getCurrentCompany();
            this.currentDesignation = application.getCandidate().getCurrentDesignation();
            this.expectedSalary = application.getCandidate().getExpectedSalary();
            this.noticePeriodDays = application.getCandidate().getNoticePeriodDays();
            this.resumeUrl = application.getCandidate().getResumeUrl();
        }
        if (application.getJob() != null) {
            this.jobId = application.getJob().getId();
            this.jobTitle = application.getJob().getTitle();
            this.department = application.getJob().getDepartment();
        }
        this.status = application.getStatus();
        this.rejectionReason = application.getRejectionReason();
        this.appliedAt = application.getAppliedAt();
        this.updatedAt = application.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidatePhone() { return candidatePhone; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }

    public Double getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Double experienceYears) { this.experienceYears = experienceYears; }

    public String getCurrentCompany() { return currentCompany; }
    public void setCurrentCompany(String currentCompany) { this.currentCompany = currentCompany; }

    public String getCurrentDesignation() { return currentDesignation; }
    public void setCurrentDesignation(String currentDesignation) { this.currentDesignation = currentDesignation; }

    public BigDecimal getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(BigDecimal expectedSalary) { this.expectedSalary = expectedSalary; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
