package com.example.ems.finance.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_finance_onboardings")
public class EmployeeFinanceOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SENT_BACK, DRAFT

    // Bank Details
    private String bankName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankVerificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED
    private String bankVerificationNotes;

    // PAN Details
    private String panNumber;
    private String panVerificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED
    private String panVerificationNotes;

    // PF/UAN Details
    private String uanNumber;
    private String uanVerificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED
    private String uanVerificationNotes;

    // Salary Structure
    private BigDecimal basicSalary = BigDecimal.ZERO;
    private BigDecimal hra = BigDecimal.ZERO;
    private BigDecimal allowances = BigDecimal.ZERO;
    private BigDecimal monthlyCtc = BigDecimal.ZERO;
    private Boolean salaryStructureAssigned = false;

    // Payroll Status
    private Boolean payrollActivated = false;
    private LocalDateTime payrollActivatedAt;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Integer optVersion;

    public EmployeeFinanceOnboarding() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getBankIfsc() {
        return bankIfsc;
    }

    public void setBankIfsc(String bankIfsc) {
        this.bankIfsc = bankIfsc;
    }

    public String getBankVerificationStatus() {
        return bankVerificationStatus;
    }

    public void setBankVerificationStatus(String bankVerificationStatus) {
        this.bankVerificationStatus = bankVerificationStatus;
    }

    public String getBankVerificationNotes() {
        return bankVerificationNotes;
    }

    public void setBankVerificationNotes(String bankVerificationNotes) {
        this.bankVerificationNotes = bankVerificationNotes;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getPanVerificationStatus() {
        return panVerificationStatus;
    }

    public void setPanVerificationStatus(String panVerificationStatus) {
        this.panVerificationStatus = panVerificationStatus;
    }

    public String getPanVerificationNotes() {
        return panVerificationNotes;
    }

    public void setPanVerificationNotes(String panVerificationNotes) {
        this.panVerificationNotes = panVerificationNotes;
    }

    public String getUanNumber() {
        return uanNumber;
    }

    public void setUanNumber(String uanNumber) {
        this.uanNumber = uanNumber;
    }

    public String getUanVerificationStatus() {
        return uanVerificationStatus;
    }

    public void setUanVerificationStatus(String uanVerificationStatus) {
        this.uanVerificationStatus = uanVerificationStatus;
    }

    public String getUanVerificationNotes() {
        return uanVerificationNotes;
    }

    public void setUanVerificationNotes(String uanVerificationNotes) {
        this.uanVerificationNotes = uanVerificationNotes;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getMonthlyCtc() {
        return monthlyCtc;
    }

    public void setMonthlyCtc(BigDecimal monthlyCtc) {
        this.monthlyCtc = monthlyCtc;
    }

    public Boolean getSalaryStructureAssigned() {
        return salaryStructureAssigned;
    }

    public void setSalaryStructureAssigned(Boolean salaryStructureAssigned) {
        this.salaryStructureAssigned = salaryStructureAssigned;
    }

    public Boolean getPayrollActivated() {
        return payrollActivated;
    }

    public void setPayrollActivated(Boolean payrollActivated) {
        this.payrollActivated = payrollActivated;
    }

    public LocalDateTime getPayrollActivatedAt() {
        return payrollActivatedAt;
    }

    public void setPayrollActivatedAt(LocalDateTime payrollActivatedAt) {
        this.payrollActivatedAt = payrollActivatedAt;
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

    public Integer getOptVersion() {
        return optVersion;
    }

    public void setOptVersion(Integer optVersion) {
        this.optVersion = optVersion;
    }
}
