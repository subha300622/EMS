package com.example.ems.finance.dto;

public class ReimburseExpenseRequest {
    private String paymentMode;
    private String transactionReference;

    public ReimburseExpenseRequest() {}

    public ReimburseExpenseRequest(String paymentMode, String transactionReference) {
        this.paymentMode = paymentMode;
        this.transactionReference = transactionReference;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
