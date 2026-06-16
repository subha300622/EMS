package com.example.ems.appraisal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionUpdateResponse {

    private String revisionId;
    private BigDecimal incrementPercentage;
    private BigDecimal incrementAmount;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String status;

    public SalaryRevisionUpdateResponse() {}

    public SalaryRevisionUpdateResponse(String revisionId, BigDecimal incrementPercentage, BigDecimal incrementAmount, BigDecimal newSalary, LocalDate effectiveDate, String status) {
        this.revisionId = revisionId;
        this.incrementPercentage = incrementPercentage;
        this.incrementAmount = incrementAmount;
        this.newSalary = newSalary;
        this.effectiveDate = effectiveDate;
        this.status = status;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public BigDecimal getIncrementAmount() { return incrementAmount; }
    public void setIncrementAmount(BigDecimal incrementAmount) { this.incrementAmount = incrementAmount; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
