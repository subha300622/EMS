package com.example.ems.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_onboardings")
public class FinanceOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company Settings
    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyRegistrationNumber;
    private String companyTaxId;
    private String companyWebsite;

    // Bank Account
    private String bankName;
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String bankSwiftCode;

    // Tax Settings
    private String taxRegime;
    private BigDecimal taxRate;
    private String taxFinancialYear;

    // Payment Method
    private String paymentMethod;
    private String paymentCurrency;

    // Payroll Settings
    private Integer payrollCycleStartDay;
    private Integer payrollCycleEndDay;

    // Budget Settings
    private BigDecimal budgetTotal;
    private String budgetCurrency;
    private String budgetDepartmentBreakdown;

    // Status & Progress Tracking
    private String status = "DRAFT"; // DRAFT, VALIDATED, COMPLETED, ARCHIVED
    private Integer stepProgress = 0;
    private Boolean validated = false;
    private Boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FinanceOnboarding() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyRegistrationNumber() {
        return companyRegistrationNumber;
    }

    public void setCompanyRegistrationNumber(String companyRegistrationNumber) {
        this.companyRegistrationNumber = companyRegistrationNumber;
    }

    public String getCompanyTaxId() {
        return companyTaxId;
    }

    public void setCompanyTaxId(String companyTaxId) {
        this.companyTaxId = companyTaxId;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getBankSwiftCode() {
        return bankSwiftCode;
    }

    public void setBankSwiftCode(String bankSwiftCode) {
        this.bankSwiftCode = bankSwiftCode;
    }

    public String getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxFinancialYear() {
        return taxFinancialYear;
    }

    public void setTaxFinancialYear(String taxFinancialYear) {
        this.taxFinancialYear = taxFinancialYear;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public Integer getPayrollCycleStartDay() {
        return payrollCycleStartDay;
    }

    public void setPayrollCycleStartDay(Integer payrollCycleStartDay) {
        this.payrollCycleStartDay = payrollCycleStartDay;
    }

    public Integer getPayrollCycleEndDay() {
        return payrollCycleEndDay;
    }

    public void setPayrollCycleEndDay(Integer payrollCycleEndDay) {
        this.payrollCycleEndDay = payrollCycleEndDay;
    }

    public BigDecimal getBudgetTotal() {
        return budgetTotal;
    }

    public void setBudgetTotal(BigDecimal budgetTotal) {
        this.budgetTotal = budgetTotal;
    }

    public String getBudgetCurrency() {
        return budgetCurrency;
    }

    public void setBudgetCurrency(String budgetCurrency) {
        this.budgetCurrency = budgetCurrency;
    }

    public String getBudgetDepartmentBreakdown() {
        return budgetDepartmentBreakdown;
    }

    public void setBudgetDepartmentBreakdown(String budgetDepartmentBreakdown) {
        this.budgetDepartmentBreakdown = budgetDepartmentBreakdown;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStepProgress() {
        return stepProgress;
    }

    public void setStepProgress(Integer stepProgress) {
        this.stepProgress = stepProgress;
    }

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
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
