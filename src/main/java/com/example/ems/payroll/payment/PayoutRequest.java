package com.example.ems.payroll.payment;

import java.math.BigDecimal;

public class PayoutRequest {

    private final String accountNumber;
    private final String fundAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final String mode;
    private final String purpose;
    private final String idempotencyKey;
    private final String narration;

    public PayoutRequest(String accountNumber, String fundAccountId, BigDecimal amount,
                         String currency, String mode, String purpose,
                         String idempotencyKey, String narration) {
        this.accountNumber = accountNumber;
        this.fundAccountId = fundAccountId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.mode = mode != null ? mode : "NEFT";
        this.purpose = purpose != null ? purpose : "salary";
        this.idempotencyKey = idempotencyKey;
        this.narration = narration;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getFundAccountId() {
        return fundAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMode() {
        return mode;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getNarration() {
        return narration;
    }
}
