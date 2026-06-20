package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionHistoryResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;
    @Schema(example = "120000.00")
    private BigDecimal previousSalary;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;
    @Schema(example = "Personal business")
    private String reason;
    @Schema(example = "ACTIVE")
    private String status;

    public SalaryRevisionHistoryResponse() {}

    public SalaryRevisionHistoryResponse(String revisionId, LocalDate effectiveDate, BigDecimal previousSalary, BigDecimal newSalary, BigDecimal incrementPercentage, String reason, String status) {
        this.revisionId = revisionId;
        this.effectiveDate = effectiveDate;
        this.previousSalary = previousSalary;
        this.newSalary = newSalary;
        this.incrementPercentage = incrementPercentage;
        this.reason = reason;
        this.status = status;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getPreviousSalary() { return previousSalary; }
    public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
