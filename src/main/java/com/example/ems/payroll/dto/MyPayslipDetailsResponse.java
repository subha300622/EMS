package com.example.ems.payroll.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public class MyPayslipDetailsResponse {

    @Schema(example = "1")
    private Long payslipId;
    @Schema(example = "string")
    private String payslipNumber;
    private EmployeeInfo employee;
    private SalaryPeriodInfo salaryPeriod;
    private List<ComponentAmount> earnings;
    private List<ComponentAmount> deductions;
    private SummaryInfo summary;
    private PaymentDetailsInfo paymentDetails;

    public MyPayslipDetailsResponse() {}

    public MyPayslipDetailsResponse(Long payslipId, String payslipNumber, EmployeeInfo employee, SalaryPeriodInfo salaryPeriod, List<ComponentAmount> earnings, List<ComponentAmount> deductions, SummaryInfo summary, PaymentDetailsInfo paymentDetails) {
        this.payslipId = payslipId;
        this.payslipNumber = payslipNumber;
        this.employee = employee;
        this.salaryPeriod = salaryPeriod;
        this.earnings = earnings;
        this.deductions = deductions;
        this.summary = summary;
        this.paymentDetails = paymentDetails;
    }

    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }

    public String getPayslipNumber() { return payslipNumber; }
    public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public SalaryPeriodInfo getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(SalaryPeriodInfo salaryPeriod) { this.salaryPeriod = salaryPeriod; }

    public List<ComponentAmount> getEarnings() { return earnings; }
    public void setEarnings(List<ComponentAmount> earnings) { this.earnings = earnings; }

    public List<ComponentAmount> getDeductions() { return deductions; }
    public void setDeductions(List<ComponentAmount> deductions) { this.deductions = deductions; }

    public SummaryInfo getSummary() { return summary; }
    public void setSummary(SummaryInfo summary) { this.summary = summary; }

    public PaymentDetailsInfo getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetailsInfo paymentDetails) { this.paymentDetails = paymentDetails; }

    public static class EmployeeInfo {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "string")
        private String name;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "Software Engineer")
        private String designation;
        @Schema(example = "Engineering")
        private String department;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String name, String employeeCode, String designation, String department) {
            this.id = id;
            this.name = name;
            this.employeeCode = employeeCode;
            this.designation = designation;
            this.department = department;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }

    public static class SalaryPeriodInfo {
        @Schema(example = "string")
        private String month;
        @Schema(example = "2026")
        private Integer year;
        @Schema(example = "1")
        private Integer workingDays;
        @Schema(example = "1")
        private Integer paidDays;

        public SalaryPeriodInfo() {}

        public SalaryPeriodInfo(String month, Integer year, Integer workingDays, Integer paidDays) {
            this.month = month;
            this.year = year;
            this.workingDays = workingDays;
            this.paidDays = paidDays;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Integer getWorkingDays() { return workingDays; }
        public void setWorkingDays(Integer workingDays) { this.workingDays = workingDays; }

        public Integer getPaidDays() { return paidDays; }
        public void setPaidDays(Integer paidDays) { this.paidDays = paidDays; }
    }

    public static class ComponentAmount {
        @Schema(example = "string")
        private String component;
        @Schema(example = "5000.00")
        private BigDecimal amount;

        public ComponentAmount() {}

        public ComponentAmount(String component, BigDecimal amount) {
            this.component = component;
            this.amount = amount;
        }

        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class SummaryInfo {
        @Schema(example = "120000.00")
        private BigDecimal grossSalary;
        @Schema(example = "5000.00")
        private BigDecimal totalDeductions;
        @Schema(example = "120000.00")
        private BigDecimal netSalary;

        public SummaryInfo() {}

        public SummaryInfo(BigDecimal grossSalary, BigDecimal totalDeductions, BigDecimal netSalary) {
            this.grossSalary = grossSalary;
            this.totalDeductions = totalDeductions;
            this.netSalary = netSalary;
        }

        public BigDecimal getGrossSalary() { return grossSalary; }
        public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getNetSalary() { return netSalary; }
        public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    }

    public static class PaymentDetailsInfo {
        @Schema(example = "ACTIVE")
        private String status;
        @Schema(example = "string")
        private String paymentMode;
        @Schema(example = "string")
        private String transactionReference;

        public PaymentDetailsInfo() {}

        public PaymentDetailsInfo(String status, String paymentMode, String transactionReference) {
            this.status = status;
            this.paymentMode = paymentMode;
            this.transactionReference = transactionReference;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPaymentMode() { return paymentMode; }
        public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    }
}
