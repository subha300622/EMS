package com.example.ems.finance.dto;

import com.example.ems.payroll.entity.PaymentMode;

public class ProcessRequest {
    private PaymentMode paymentMode;
    private String transactionReference;

    public ProcessRequest() {}

    public ProcessRequest(PaymentMode paymentMode, String transactionReference) {
        this.paymentMode = paymentMode;
        this.transactionReference = transactionReference;
    }

    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
}
