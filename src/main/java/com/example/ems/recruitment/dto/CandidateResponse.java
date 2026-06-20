package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.recruitment.entity.Candidate;

import java.time.LocalDateTime;

public class CandidateResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "John Doe")
    private String fullName;
    @Schema(example = "john.doe@example.com")
    private String email;
    @Schema(example = "+1-555-0199")
    private String phone;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String resumeFileName;
    @Schema(example = "string")
    private String resumeUrl;
    private JobResponse job;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime appliedAt;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime updatedAt;

    public CandidateResponse() {}

    public CandidateResponse(Candidate candidate) {
        this.id = candidate.getId();
        this.fullName = candidate.getFullName();
        this.email = candidate.getEmail();
        this.phone = candidate.getPhone();
        this.status = candidate.getStatus();
        this.resumeFileName = candidate.getResumeFileName();
        this.resumeUrl = candidate.getResumeUrl();
        this.appliedAt = candidate.getAppliedAt();
        this.updatedAt = candidate.getUpdatedAt();
        if (candidate.getJob() != null) {
            this.job = new JobResponse(candidate.getJob());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public JobResponse getJob() { return job; }
    public void setJob(JobResponse job) { this.job = job; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
