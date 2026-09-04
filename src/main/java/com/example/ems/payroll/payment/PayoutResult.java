package com.example.ems.payroll.payment;

import com.example.ems.payroll.entity.PayrollPaymentStatus;

public class PayoutResult {

    private final String payoutId;
    private final PayrollPaymentStatus status;
    private final String utr;
    private final String failureReason;
    private final boolean success;

    public PayoutResult(String payoutId, PayrollPaymentStatus status, String utr, String failureReason, boolean success) {
        this.payoutId = payoutId;
        this.status = status;
        this.utr = utr;
        this.failureReason = failureReason;
        this.success = success;
    }

    public static PayoutResult success(String payoutId, PayrollPaymentStatus status, String utr) {
        return new PayoutResult(payoutId, status, utr, null, true);
    }

    public static PayoutResult failure(String failureReason) {
        return new PayoutResult(null, PayrollPaymentStatus.FAILED, null, failureReason, false);
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

    public boolean isSuccess() {
        return success;
    }
}
