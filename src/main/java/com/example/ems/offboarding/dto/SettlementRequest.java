package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SettlementRequest {

    @NotNull(message = "Offboarding ID is required")
    @Schema(example = "1")
    private Long offboardingId;

    @Schema(example = "5000.00")
    private BigDecimal gratuity = BigDecimal.ZERO;
    @Schema(example = "100.00")
    private BigDecimal severance = BigDecimal.ZERO;
    @Schema(example = "120000.00")
    private BigDecimal pendingSalary = BigDecimal.ZERO;
    @Schema(example = "5000.00")
    private BigDecimal deductions = BigDecimal.ZERO;

    public Long getOffboardingId() {
        return offboardingId;
    }

    public void setOffboardingId(Long offboardingId) {
        this.offboardingId = offboardingId;
    }

    public BigDecimal getGratuity() {
        return gratuity;
    }

    public void setGratuity(BigDecimal gratuity) {
        this.gratuity = gratuity;
    }

    public BigDecimal getSeverance() {
        return severance;
    }

    public void setSeverance(BigDecimal severance) {
        this.severance = severance;
    }

    public BigDecimal getPendingSalary() {
        return pendingSalary;
    }

    public void setPendingSalary(BigDecimal pendingSalary) {
        this.pendingSalary = pendingSalary;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }
}
