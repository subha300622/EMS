package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NewIncrementRequest {

    @Schema(example = "string")
    private String employeeId;

    @Schema(example = "1")
    private Long appraisalId; // Optional linkage to appraisal

    @Schema(example = "100.00")
    private BigDecimal incrementPercentage;

    @Schema(example = "2026-06-19")
    private LocalDate effectiveDate;

    @Schema(example = "Personal business")
    private String reason;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
