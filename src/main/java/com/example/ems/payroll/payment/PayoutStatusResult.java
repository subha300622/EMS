package com.example.ems.payroll.payment;

import com.example.ems.payroll.entity.PayrollPaymentStatus;

public class PayoutStatusResult {

    private final String payoutId;
    private final PayrollPaymentStatus status;
    private final String utr;
    private final String failureReason;

    public PayoutStatusResult(String payoutId, PayrollPaymentStatus status, String utr, String failureReason) {
        this.payoutId = payoutId;
        this.status = status;
        this.utr = utr;
        this.failureReason = failureReason;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public PayrollPaymentStatus getStatus() {
        return status;
    }

    public String getUtr() {
        return utr;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
