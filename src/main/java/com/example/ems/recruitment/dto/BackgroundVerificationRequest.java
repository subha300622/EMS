package com.example.ems.recruitment.dto;

import jakarta.validation.constraints.NotNull;

public class BackgroundVerificationRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private String verificationAgency;

    private String status;

    private String reportsMetadata;

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getVerificationAgency() {
        return verificationAgency;
    }

    public void setVerificationAgency(String verificationAgency) {
        this.verificationAgency = verificationAgency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportsMetadata() {
        return reportsMetadata;
    }

    public void setReportsMetadata(String reportsMetadata) {
        this.reportsMetadata = reportsMetadata;
    }
}
