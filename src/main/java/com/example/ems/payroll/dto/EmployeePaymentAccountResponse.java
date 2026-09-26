package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.EmployeePaymentAccount;
import com.example.ems.payroll.entity.PaymentAccountType;
import com.example.ems.payroll.entity.PaymentProviderType;

import java.time.LocalDateTime;

public class EmployeePaymentAccountResponse {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private PaymentProviderType provider;
    private String contactId;
    private String fundAccountId;
    private PaymentAccountType accountType;
    private String maskedAccountNumber;
    private String ifscCode;
    private String beneficiaryName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeePaymentAccountResponse() {}

    public static EmployeePaymentAccountResponse fromEntity(EmployeePaymentAccount account) {
        if (account == null) return null;
        EmployeePaymentAccountResponse res = new EmployeePaymentAccountResponse();
        res.setId(account.getId());
        res.setOrganizationId(account.getOrganizationId());
        res.setEmployeeId(account.getEmployeeId());
        res.setProvider(account.getProvider());
        res.setContactId(account.getContactId());
        res.setFundAccountId(account.getFundAccountId());
        res.setAccountType(account.getAccountType());
        res.setMaskedAccountNumber(maskAccountNumber(account.getAccountNumber()));
        res.setIfscCode(account.getIfscCode());
        res.setBeneficiaryName(account.getBeneficiaryName());
        res.setActive(account.getActive());
        res.setCreatedAt(account.getCreatedAt());
        res.setUpdatedAt(account.getUpdatedAt());
        return res;
    }

    private static String maskAccountNumber(String acc) {
        if (acc == null || acc.isEmpty()) return "";
        if (acc.length() <= 4) return "****";
        return "****" + acc.substring(acc.length() - 4);
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

    public String getMaskedAccountNumber() {
        return maskedAccountNumber;
    }

    public void setMaskedAccountNumber(String maskedAccountNumber) {
        this.maskedAccountNumber = maskedAccountNumber;
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
