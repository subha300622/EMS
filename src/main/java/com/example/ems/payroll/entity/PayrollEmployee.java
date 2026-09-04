package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payroll_employees",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_payroll_employee",
            columnNames = {"payroll_run_id", "employee_id"}
        )
    }
)
public class PayrollEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "payroll_run_id", nullable = false)
    private Long payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(name = "benefits_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal benefitsAmount = BigDecimal.ZERO;

    @Column(name = "deductions_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal deductionsAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayrollEmployeeStatus status = PayrollEmployeeStatus.CALCULATED;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PayrollEmployee() {}

    public PayrollEmployee(Long organizationId, Long payrollRunId, Long employeeId, String employeeName,
                           String employeeCode, BigDecimal grossAmount, BigDecimal benefitsAmount,
                           BigDecimal deductionsAmount, BigDecimal netAmount, String currency,
                           PayrollEmployeeStatus status, LocalDate calculationDate, String errorMessage) {
        this.organizationId = organizationId;
        this.payrollRunId = payrollRunId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.grossAmount = grossAmount != null ? grossAmount : BigDecimal.ZERO;
        this.benefitsAmount = benefitsAmount != null ? benefitsAmount : BigDecimal.ZERO;
        this.deductionsAmount = deductionsAmount != null ? deductionsAmount : BigDecimal.ZERO;
        this.netAmount = netAmount != null ? netAmount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
        this.status = status != null ? status : PayrollEmployeeStatus.CALCULATED;
        this.calculationDate = calculationDate != null ? calculationDate : LocalDate.now();
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currency == null) this.currency = "INR";
        if (this.grossAmount == null) this.grossAmount = BigDecimal.ZERO;
        if (this.benefitsAmount == null) this.benefitsAmount = BigDecimal.ZERO;
        if (this.deductionsAmount == null) this.deductionsAmount = BigDecimal.ZERO;
        if (this.netAmount == null) this.netAmount = BigDecimal.ZERO;
        if (this.status == null) this.status = PayrollEmployeeStatus.CALCULATED;
        if (this.calculationDate == null) this.calculationDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

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

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getBenefitsAmount() {
        return benefitsAmount;
    }

    public void setBenefitsAmount(BigDecimal benefitsAmount) {
        this.benefitsAmount = benefitsAmount;
    }

    public BigDecimal getDeductionsAmount() {
        return deductionsAmount;
    }

    public void setDeductionsAmount(BigDecimal deductionsAmount) {
        this.deductionsAmount = deductionsAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayrollEmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollEmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
