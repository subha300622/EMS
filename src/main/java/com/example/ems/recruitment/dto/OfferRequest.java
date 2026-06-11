package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Candidate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OfferRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotNull(message = "Offered salary is required")
    @Positive(message = "Offered salary must be positive")
    private BigDecimal offeredSalary;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public BigDecimal getOfferedSalary() { return offeredSalary; }
    public void setOfferedSalary(BigDecimal offeredSalary) { this.offeredSalary = offeredSalary; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
