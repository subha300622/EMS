package com.example.ems.appraisal.dto;

import java.time.LocalDate;

public class ApplyIncrementRequestDto {
    private LocalDate effectiveDate;
    private String remarks;

    public ApplyIncrementRequestDto() {}

    public ApplyIncrementRequestDto(LocalDate effectiveDate, String remarks) {
        this.effectiveDate = effectiveDate;
        this.remarks = remarks;
    }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
