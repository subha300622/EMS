package com.example.ems.leave.dto;

import java.util.Objects;

public class LeavePeriodSummaryDto {

    private Double paidDays = 0.0;
    private Double lopDays = 0.0;
    private Double encashmentDays = 0.0;

    public LeavePeriodSummaryDto() {}

    public LeavePeriodSummaryDto(Double paidDays, Double lopDays, Double encashmentDays) {
        this.paidDays = paidDays != null ? paidDays : 0.0;
        this.lopDays = lopDays != null ? lopDays : 0.0;
        this.encashmentDays = encashmentDays != null ? encashmentDays : 0.0;
    }

    public Double getPaidDays() {
        return paidDays;
    }

    public void setPaidDays(Double paidDays) {
        this.paidDays = paidDays != null ? paidDays : 0.0;
    }

    public Double getLopDays() {
        return lopDays;
    }

    public void setLopDays(Double lopDays) {
        this.lopDays = lopDays != null ? lopDays : 0.0;
    }

    public Double getEncashmentDays() {
        return encashmentDays;
    }

    public void setEncashmentDays(Double encashmentDays) {
        this.encashmentDays = encashmentDays != null ? encashmentDays : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeavePeriodSummaryDto that = (LeavePeriodSummaryDto) o;
        return Objects.equals(paidDays, that.paidDays) &&
               Objects.equals(lopDays, that.lopDays) &&
               Objects.equals(encashmentDays, that.encashmentDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paidDays, lopDays, encashmentDays);
    }

    @Override
    public String toString() {
        return "LeavePeriodSummaryDto{" +
                "paidDays=" + paidDays +
                ", lopDays=" + lopDays +
                ", encashmentDays=" + encashmentDays +
                '}';
    }
}
