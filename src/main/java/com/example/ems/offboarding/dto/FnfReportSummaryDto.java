package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "F&F Settlement Report Item")
public class FnfReportSummaryDto {

    @Schema(description = "F&F settlement record ID", example = "8001")
    private Long fnfId;

    @Schema(description = "Exit record ID", example = "5001")
    private Long exitId;

    @Schema(description = "Employee ID", example = "1025")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "Sarah Jenkins")
    private String employeeName;

    @Schema(description = "Employee official code", example = "EMP-1025")
    private String employeeCode;

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @Schema(description = "Employee job designation", example = "Senior Software Engineer")
    private String designation;

    @Schema(description = "F&F processing/lifecycle status", example = "PAYMENT_RELEASED")
    private String status;

    @Schema(description = "Total gross earnings", example = "104000.00")
    private BigDecimal totalEarnings;

    @Schema(description = "Total aggregate deductions", example = "17233.33")
    private BigDecimal totalDeductions;

    @Schema(description = "Net payable settlement amount", example = "86766.67")
    private BigDecimal netSettlement;

    @Schema(description = "Actual amount disbursed to employee", example = "86766.67")
    private BigDecimal paidAmount;

    @Schema(description = "Date of payment release", example = "2026-10-20")
    private LocalDate paymentDate;

    @Schema(description = "Bank transaction UTR or cheque reference", example = "TXN-202610200001")
    private String paymentReference;

    @Schema(description = "Employee confirmed last working date", example = "2026-10-17")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastWorkingDate;

    public FnfReportSummaryDto() {}

    public Long getFnfId() { return fnfId; }
    public void setFnfId(Long fnfId) { this.fnfId = fnfId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }

    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

    public BigDecimal getNetSettlement() { return netSettlement; }
    public void setNetSettlement(BigDecimal netSettlement) { this.netSettlement = netSettlement; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }
}
