package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Offer;
import com.example.ems.recruitment.entity.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OfferResponse {

    private Long id;
    private Long organizationId;
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String offerNumber;
    private String designation;
    private BigDecimal annualSalary;
    private LocalDate joiningDate;
    private Integer probationMonths;
    private OfferStatus status;
    private String acceptanceToken;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OfferResponse() {}

    public OfferResponse(Offer offer) {
        this.id = offer.getId();
        this.organizationId = offer.getOrganizationId();
        if (offer.getApplication() != null) {
            this.applicationId = offer.getApplication().getId();
            if (offer.getApplication().getCandidate() != null) {
                this.candidateName = offer.getApplication().getCandidate().getFullName();
                this.candidateEmail = offer.getApplication().getCandidate().getEmail();
            }
            if (offer.getApplication().getJob() != null) {
                this.jobTitle = offer.getApplication().getJob().getTitle();
            }
        }
        this.offerNumber = offer.getOfferNumber();
        this.designation = offer.getDesignation();
        this.annualSalary = offer.getAnnualSalary();
        this.joiningDate = offer.getJoiningDate();
        this.probationMonths = offer.getProbationMonths();
        this.status = offer.getStatus();
        this.acceptanceToken = offer.getAcceptanceToken();
        this.sentAt = offer.getSentAt();
        this.acceptedAt = offer.getAcceptedAt();
        this.createdAt = offer.getCreatedAt();
        this.updatedAt = offer.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getOfferNumber() { return offerNumber; }
    public void setOfferNumber(String offerNumber) { this.offerNumber = offerNumber; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Integer getProbationMonths() { return probationMonths; }
    public void setProbationMonths(Integer probationMonths) { this.probationMonths = probationMonths; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public String getAcceptanceToken() { return acceptanceToken; }
    public void setAcceptanceToken(String acceptanceToken) { this.acceptanceToken = acceptanceToken; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
