package com.example.ems.finance.dto;

public class ReimburseExpenseResponse {
    private Long expenseId;
    private String status;
    private String paymentMode;
    private String transactionReference;

    public ReimburseExpenseResponse() {}

    public ReimburseExpenseResponse(Long expenseId, String status, String paymentMode, String transactionReference) {
        this.expenseId = expenseId;
        this.status = status;
        this.paymentMode = paymentMode;
        this.transactionReference = transactionReference;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
