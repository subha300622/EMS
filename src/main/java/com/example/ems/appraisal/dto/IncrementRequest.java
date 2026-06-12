package com.example.ems.appraisal.dto;




import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class IncrementRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private Long appraisalId; // Optional linkage to appraisal

    @NotNull(message = "Increment percentage is required")
    @Positive(message = "Increment percentage must be positive")
    private BigDecimal incrementPercentage;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public BigDecimal getIncrementPercentage() { return incrementPercentage; }
    public void setIncrementPercentage(BigDecimal incrementPercentage) { this.incrementPercentage = incrementPercentage; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
