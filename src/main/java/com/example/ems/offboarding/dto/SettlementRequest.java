package com.example.ems.offboarding.dto;

import com.example.ems.offboarding.entity.Offboarding;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SettlementRequest {

    @NotNull(message = "Offboarding ID is required")
    private Long offboardingId;

    private BigDecimal gratuity = BigDecimal.ZERO;
    private BigDecimal severance = BigDecimal.ZERO;
    private BigDecimal pendingSalary = BigDecimal.ZERO;
    private BigDecimal deductions = BigDecimal.ZERO;

    public Long getOffboardingId() { return offboardingId; }
    public void setOffboardingId(Long offboardingId) { this.offboardingId = offboardingId; }

    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getSeverance() { return severance; }
    public void setSeverance(BigDecimal severance) { this.severance = severance; }

    public BigDecimal getPendingSalary() { return pendingSalary; }
    public void setPendingSalary(BigDecimal pendingSalary) { this.pendingSalary = pendingSalary; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
}
