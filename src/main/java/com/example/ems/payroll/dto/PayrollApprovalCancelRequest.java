package com.example.ems.payroll.dto;

public class PayrollApprovalCancelRequest {

    private String reason;

    public PayrollApprovalCancelRequest() {}

    public PayrollApprovalCancelRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
