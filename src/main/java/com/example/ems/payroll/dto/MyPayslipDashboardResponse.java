package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MyPayslipDashboardResponse {

    private EmployeeInfo employee;
    private SalaryOverviewInfo salaryOverview;
    private LatestPayrollInfo latestPayroll;
    private StatisticsInfo statistics;

    public MyPayslipDashboardResponse() {}

    public MyPayslipDashboardResponse(EmployeeInfo employee, SalaryOverviewInfo salaryOverview, LatestPayrollInfo latestPayroll, StatisticsInfo statistics) {
        this.employee = employee;
        this.salaryOverview = salaryOverview;
        this.latestPayroll = latestPayroll;
        this.statistics = statistics;
    }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public SalaryOverviewInfo getSalaryOverview() { return salaryOverview; }
    public void setSalaryOverview(SalaryOverviewInfo salaryOverview) { this.salaryOverview = salaryOverview; }

    public LatestPayrollInfo getLatestPayroll() { return latestPayroll; }
    public void setLatestPayroll(LatestPayrollInfo latestPayroll) { this.latestPayroll = latestPayroll; }

    public StatisticsInfo getStatistics() { return statistics; }
    public void setStatistics(StatisticsInfo statistics) { this.statistics = statistics; }

    public static class EmployeeInfo {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "string")
        private String name;
        @Schema(example = "Software Engineer")
        private String designation;
        @Schema(example = "Engineering")
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String employeeCode, String name, String designation, String department) {
            this.id = id;
            this.employeeCode = employeeCode;
            this.name = name;
            this.designation = designation;
            this.department = department;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }

    public static class SalaryOverviewInfo {
        private CTCInfo currentCTC;
        @Schema(example = "2026-06-19")
        private LocalDate lastRevisionDate;

        public SalaryOverviewInfo() {}

        public SalaryOverviewInfo(CTCInfo currentCTC, LocalDate lastRevisionDate) {
            this.currentCTC = currentCTC;
            this.lastRevisionDate = lastRevisionDate;
        }

        public CTCInfo getCurrentCTC() { return currentCTC; }
        public void setCurrentCTC(CTCInfo currentCTC) { this.currentCTC = currentCTC; }

        public LocalDate getLastRevisionDate() { return lastRevisionDate; }
        public void setLastRevisionDate(LocalDate lastRevisionDate) { this.lastRevisionDate = lastRevisionDate; }
    }

    public static class CTCInfo {
        @Schema(example = "5000.00")
        private BigDecimal amount;
        @Schema(example = "string")
        private String currency = "INR";
        @Schema(example = "string")
        private String formatted;

        public CTCInfo() {}

        public CTCInfo(BigDecimal amount, String currency, String formatted) {
            this.amount = amount;
            this.currency = currency;
            this.formatted = formatted;
        }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getFormatted() { return formatted; }
        public void setFormatted(String formatted) { this.formatted = formatted; }
    }

    public static class LatestPayrollInfo {
        @Schema(example = "string")
        private String payPeriod;
        @Schema(example = "120000.00")
        private BigDecimal grossSalary;
        @Schema(example = "5000.00")
        private BigDecimal totalDeductions;
        @Schema(example = "120000.00")
        private BigDecimal netSalary;
        @Schema(example = "2026-06-19")
        private LocalDate salaryCreditedDate;
        @Schema(example = "ACTIVE")
        private String paymentStatus;

        public LatestPayrollInfo() {}

        public LatestPayrollInfo(String payPeriod, BigDecimal grossSalary, BigDecimal totalDeductions, BigDecimal netSalary, LocalDate salaryCreditedDate, String paymentStatus) {
            this.payPeriod = payPeriod;
            this.grossSalary = grossSalary;
            this.totalDeductions = totalDeductions;
            this.netSalary = netSalary;
            this.salaryCreditedDate = salaryCreditedDate;
            this.paymentStatus = paymentStatus;
        }

        public String getPayPeriod() { return payPeriod; }
        public void setPayPeriod(String payPeriod) { this.payPeriod = payPeriod; }

        public BigDecimal getGrossSalary() { return grossSalary; }
        public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getNetSalary() { return netSalary; }
        public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }

        public LocalDate getSalaryCreditedDate() { return salaryCreditedDate; }
        public void setSalaryCreditedDate(LocalDate salaryCreditedDate) { this.salaryCreditedDate = salaryCreditedDate; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    public static class StatisticsInfo {
        @Schema(example = "1")
        private int availablePayslips;
        @Schema(example = "string")
        private String financialYear;

        public StatisticsInfo() {}

        public StatisticsInfo(int availablePayslips, String financialYear) {
            this.availablePayslips = availablePayslips;
            this.financialYear = financialYear;
        }

        public int getAvailablePayslips() { return availablePayslips; }
        public void setAvailablePayslips(int availablePayslips) { this.availablePayslips = availablePayslips; }

        public String getFinancialYear() { return financialYear; }
        public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
    }
}
