package com.example.ems.offboarding.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SettlementDetailsResponse {

    @Schema(example = "120000.00")
    private BigDecimal salary;
    @Schema(example = "5000.00")
    private BigDecimal gratuity;
    @Schema(example = "100.00")
    private BigDecimal leaveEncashment;
    @Schema(example = "100.00")
    private BigDecimal reimbursements;
    @Schema(example = "5000.00")
    private BigDecimal deductions;
    @Schema(example = "5000.00")
    private BigDecimal netPayableAmount;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "2026-06-19")
    private LocalDate expectedSettlementDate;

    public SettlementDetailsResponse() {}

    public SettlementDetailsResponse(BigDecimal salary, BigDecimal gratuity, BigDecimal leaveEncashment, BigDecimal reimbursements, BigDecimal deductions, BigDecimal netPayableAmount, String status, LocalDate expectedSettlementDate) {
        this.salary = salary;
        this.gratuity = gratuity;
        this.leaveEncashment = leaveEncashment;
        this.reimbursements = reimbursements;
        this.deductions = deductions;
        this.netPayableAmount = netPayableAmount;
        this.status = status;
        this.expectedSettlementDate = expectedSettlementDate;
    }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getLeaveEncashment() { return leaveEncashment; }
    public void setLeaveEncashment(BigDecimal leaveEncashment) { this.leaveEncashment = leaveEncashment; }

    public BigDecimal getReimbursements() { return reimbursements; }
    public void setReimbursements(BigDecimal reimbursements) { this.reimbursements = reimbursements; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public BigDecimal getNetPayableAmount() { return netPayableAmount; }
    public void setNetPayableAmount(BigDecimal netPayableAmount) { this.netPayableAmount = netPayableAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getExpectedSettlementDate() { return expectedSettlementDate; }
    public void setExpectedSettlementDate(LocalDate expectedSettlementDate) { this.expectedSettlementDate = expectedSettlementDate; }
}
