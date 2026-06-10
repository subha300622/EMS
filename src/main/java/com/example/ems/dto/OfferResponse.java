package com.example.ems.dto;

import com.example.ems.entity.Offer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OfferResponse {
    private Long id;
    private BigDecimal offeredSalary;
    private LocalDate startDate;
    private String status;
    private CandidateResponse candidate;
    private JobResponse job;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;

    public OfferResponse() {}

    public OfferResponse(Offer offer) {
        this.id = offer.getId();
        this.offeredSalary = offer.getOfferedSalary();
        this.startDate = offer.getStartDate();
        this.status = offer.getStatus();
        this.sentAt = offer.getSentAt();
        this.updatedAt = offer.getUpdatedAt();
        if (offer.getCandidate() != null) {
            this.candidate = new CandidateResponse(offer.getCandidate());
        }
        if (offer.getJob() != null) {
            this.job = new JobResponse(offer.getJob());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getOfferedSalary() { return offeredSalary; }
    public void setOfferedSalary(BigDecimal offeredSalary) { this.offeredSalary = offeredSalary; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CandidateResponse getCandidate() { return candidate; }
    public void setCandidate(CandidateResponse candidate) { this.candidate = candidate; }

    public JobResponse getJob() { return job; }
    public void setJob(JobResponse job) { this.job = job; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
