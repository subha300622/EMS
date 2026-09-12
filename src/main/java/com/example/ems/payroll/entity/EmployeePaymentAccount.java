package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "employee_payment_accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_payment_acc", columnNames = {"employee_id"})
    }
)
public class EmployeePaymentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "employee_id", nullable = false, unique = true)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProviderType provider = PaymentProviderType.RAZORPAYX;

    @Column(name = "contact_id", length = 100)
    private String contactId;

    @Column(name = "fund_account_id", length = 100)
    private String fundAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private PaymentAccountType accountType = PaymentAccountType.BANK_ACCOUNT;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 30)
    private String ifscCode;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EmployeePaymentAccount() {}

    public EmployeePaymentAccount(Long organizationId, Long employeeId, PaymentProviderType provider,
                                  String contactId, String fundAccountId, PaymentAccountType accountType,
                                  String accountNumber, String ifscCode, String beneficiaryName) {
        this.organizationId = organizationId;
        this.employeeId = employeeId;
        this.provider = provider != null ? provider : PaymentProviderType.RAZORPAYX;
        this.contactId = contactId;
        this.fundAccountId = fundAccountId;
        this.accountType = accountType != null ? accountType : PaymentAccountType.BANK_ACCOUNT;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.beneficiaryName = beneficiaryName;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.provider == null) this.provider = PaymentProviderType.RAZORPAYX;
        if (this.accountType == null) this.accountType = PaymentAccountType.BANK_ACCOUNT;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public PaymentProviderType getProvider() {
        return provider;
    }

    public void setProvider(PaymentProviderType provider) {
        this.provider = provider;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getFundAccountId() {
        return fundAccountId;
    }

    public void setFundAccountId(String fundAccountId) {
        this.fundAccountId = fundAccountId;
    }

    public PaymentAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(PaymentAccountType accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
