package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OfferRequest {

    @NotNull(message = "Candidate ID is required")
    @Schema(example = "1")
    private Long candidateId;

    @NotNull(message = "Offered salary is required")
    @Positive(message = "Offered salary must be positive")
    @Schema(example = "120000.00")
    private BigDecimal offeredSalary;

    @NotNull(message = "Start date is required")
    @Schema(example = "2026-06-19")
    private LocalDate startDate;

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public BigDecimal getOfferedSalary() { return offeredSalary; }
    public void setOfferedSalary(BigDecimal offeredSalary) { this.offeredSalary = offeredSalary; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
}
