package com.example.ems.payroll.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PayrollRunCreateRequest {

    @NotNull(message = "periodStart is required")
    private LocalDate periodStart;

    @NotNull(message = "periodEnd is required")
    private LocalDate periodEnd;

    private String currency = "INR";

    public PayrollRunCreateRequest() {}

    public PayrollRunCreateRequest(LocalDate periodStart, LocalDate periodEnd) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = "INR";
    }

    public PayrollRunCreateRequest(LocalDate periodStart, LocalDate periodEnd, String currency) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currency = currency != null ? currency : "INR";
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
