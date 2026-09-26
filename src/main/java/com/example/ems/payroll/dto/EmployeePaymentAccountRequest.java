package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PaymentAccountType;
import com.example.ems.payroll.entity.PaymentProviderType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class EmployeePaymentAccountRequest {

    private PaymentProviderType provider = PaymentProviderType.RAZORPAYX;
    private PaymentAccountType accountType = PaymentAccountType.BANK_ACCOUNT;

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;

    @NotBlank(message = "ifscCode is required")
    @JsonAlias({"ifsc", "ifscCode"})
    private String ifscCode;

    @NotBlank(message = "beneficiaryName is required")
    @JsonAlias({"accountHolderName", "beneficiaryName"})
    private String beneficiaryName;

    public EmployeePaymentAccountRequest() {}

    public EmployeePaymentAccountRequest(PaymentProviderType provider, PaymentAccountType accountType,
                                         String accountNumber, String ifscCode, String beneficiaryName) {
        this.provider = provider != null ? provider : PaymentProviderType.RAZORPAYX;
        this.accountType = accountType != null ? accountType : PaymentAccountType.BANK_ACCOUNT;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.beneficiaryName = beneficiaryName;
    }

    public PaymentProviderType getProvider() {
        return provider;
    }

    public void setProvider(PaymentProviderType provider) {
        this.provider = provider;
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
}
