package com.example.ems.payroll.dto;

import java.math.BigDecimal;

public class OvertimePeriodSummaryDto {

    private double approvedHours;
    private BigDecimal hourlyRate = BigDecimal.ZERO;
    private double multiplier = 1.5;
    private BigDecimal amount = BigDecimal.ZERO;

    public OvertimePeriodSummaryDto() {}

    public OvertimePeriodSummaryDto(double approvedHours, BigDecimal hourlyRate, double multiplier, BigDecimal amount) {
        this.approvedHours = approvedHours;
        this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
        this.multiplier = multiplier;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public double getApprovedHours() {
        return approvedHours;
    }

    public void setApprovedHours(double approvedHours) {
        this.approvedHours = approvedHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
