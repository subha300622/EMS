package com.example.ems.expense.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MyExpenseDashboardResponse {
    private EmployeeInfo employee;
    private SummaryInfo summary;
    @Schema(example = "string")
    private String financialYear;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime lastUpdatedAt;

    public MyExpenseDashboardResponse() {}

    public MyExpenseDashboardResponse(EmployeeInfo employee, SummaryInfo summary, String financialYear, LocalDateTime lastUpdatedAt) {
        this.employee = employee;
        this.summary = summary;
        this.financialYear = financialYear;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }

    public SummaryInfo getSummary() {
        return summary;
    }

    public void setSummary(SummaryInfo summary) {
        this.summary = summary;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public static class EmployeeInfo {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "Engineering")
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long employeeId, String employeeCode, String fullName, String department) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.fullName = fullName;
            this.department = department;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }

    public static class SummaryInfo {
        @Schema(example = "1")
        private int totalClaims;
        @Schema(example = "1")
        private int pendingApproval;
        @Schema(example = "1")
        private int approvedClaims;
        @Schema(example = "1")
        private int rejectedClaims;
        @Schema(example = "1")
        private int reimbursedClaims;
        @Schema(example = "5000.00")
        private BigDecimal totalClaimAmount;
        @Schema(example = "5000.00")
        private BigDecimal pendingAmount;
        @Schema(example = "5000.00")
        private BigDecimal approvedAmount;
        @Schema(example = "5000.00")
        private BigDecimal reimbursedAmount;
        @Schema(example = "string")
        private String currency;

        public SummaryInfo() {}

        public SummaryInfo(int totalClaims, int pendingApproval, int approvedClaims, int rejectedClaims, int reimbursedClaims, BigDecimal totalClaimAmount, BigDecimal pendingAmount, BigDecimal approvedAmount, BigDecimal reimbursedAmount, String currency) {
            this.totalClaims = totalClaims;
            this.pendingApproval = pendingApproval;
            this.approvedClaims = approvedClaims;
            this.rejectedClaims = rejectedClaims;
            this.reimbursedClaims = reimbursedClaims;
            this.totalClaimAmount = totalClaimAmount;
            this.pendingAmount = pendingAmount;
            this.approvedAmount = approvedAmount;
            this.reimbursedAmount = reimbursedAmount;
            this.currency = currency;
        }

        public int getTotalClaims() { return totalClaims; }
        public void setTotalClaims(int totalClaims) { this.totalClaims = totalClaims; }

        public int getPendingApproval() { return pendingApproval; }
        public void setPendingApproval(int pendingApproval) { this.pendingApproval = pendingApproval; }

        public int getApprovedClaims() { return approvedClaims; }
        public void setApprovedClaims(int approvedClaims) { this.approvedClaims = approvedClaims; }

        public int getRejectedClaims() { return rejectedClaims; }
        public void setRejectedClaims(int rejectedClaims) { this.rejectedClaims = rejectedClaims; }

        public int getReimbursedClaims() { return reimbursedClaims; }
        public void setReimbursedClaims(int reimbursedClaims) { this.reimbursedClaims = reimbursedClaims; }

        public BigDecimal getTotalClaimAmount() { return totalClaimAmount; }
        public void setTotalClaimAmount(BigDecimal totalClaimAmount) { this.totalClaimAmount = totalClaimAmount; }

        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }

        public BigDecimal getApprovedAmount() { return approvedAmount; }
        public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

        public BigDecimal getReimbursedAmount() { return reimbursedAmount; }
        public void setReimbursementAmount(BigDecimal reimbursedAmount) { this.reimbursedAmount = reimbursedAmount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
