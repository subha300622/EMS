package com.example.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CandidateRequest {

    @NotBlank(message = "Candidate full name is required")
    private String fullName;

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Candidate email must be a valid email address")
    private String email;

    private String phone;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
}
