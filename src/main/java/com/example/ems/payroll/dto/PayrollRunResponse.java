package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollRunResponse {

    private Long id;
    private Long organizationId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private PayrollRunStatus status;
    private Integer totalEmployees;
    private Integer processedEmployees;
    private BigDecimal totalGross;
    private BigDecimal totalBenefits;
    private BigDecimal totalDeductions;
    private BigDecimal totalNet;
    private String currency;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime finalizedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PayrollRunResponse() {}

    public static PayrollRunResponse fromEntity(PayrollRun run) {
        if (run == null) return null;
        PayrollRunResponse res = new PayrollRunResponse();
        res.setId(run.getId());
        res.setOrganizationId(run.getOrganizationId());
        res.setPeriodStart(run.getPeriodStart());
        res.setPeriodEnd(run.getPeriodEnd());
        res.setStatus(run.getStatus());
        res.setTotalEmployees(run.getTotalEmployees());
        res.setProcessedEmployees(run.getProcessedEmployees());
        res.setTotalGross(run.getTotalGross());
        res.setTotalBenefits(run.getTotalBenefits());
        res.setTotalDeductions(run.getTotalDeductions());
        res.setTotalNet(run.getTotalNet());
        res.setCurrency(run.getCurrency());
        res.setCreatedBy(run.getCreatedBy());
        res.setUpdatedBy(run.getUpdatedBy());
        res.setFinalizedAt(run.getFinalizedAt());
        res.setCreatedAt(run.getCreatedAt());
        res.setUpdatedAt(run.getUpdatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    public PayrollRunStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollRunStatus status) {
        this.status = status;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Integer getProcessedEmployees() {
        return processedEmployees;
    }

    public void setProcessedEmployees(Integer processedEmployees) {
        this.processedEmployees = processedEmployees;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(BigDecimal totalGross) {
        this.totalGross = totalGross;
    }

    public BigDecimal getTotalBenefits() {
        return totalBenefits;
    }

    public void setTotalBenefits(BigDecimal totalBenefits) {
        this.totalBenefits = totalBenefits;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public void setTotalNet(BigDecimal totalNet) {
        this.totalNet = totalNet;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
