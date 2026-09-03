package com.example.ems.payroll.dto;

import java.util.List;

public class PaymentExecutionResponse {

    private Long payrollRunId;
    private Integer totalPayments;
    private Integer successfulDispatches;
    private Integer failedDispatches;
    private String runStatus;
    private List<PayrollPaymentResponse> payments;

    public PaymentExecutionResponse() {}

    public PaymentExecutionResponse(Long payrollRunId, Integer totalPayments,
                                    Integer successfulDispatches, Integer failedDispatches,
                                    String runStatus, List<PayrollPaymentResponse> payments) {
        this.payrollRunId = payrollRunId;
        this.totalPayments = totalPayments;
        this.successfulDispatches = successfulDispatches;
        this.failedDispatches = failedDispatches;
        this.runStatus = runStatus;
        this.payments = payments;
    }

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public Integer getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(Integer totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Integer getSuccessfulDispatches() {
        return successfulDispatches;
    }

    public void setSuccessfulDispatches(Integer successfulDispatches) {
        this.successfulDispatches = successfulDispatches;
    }

    public Integer getFailedDispatches() {
        return failedDispatches;
    }

    public void setFailedDispatches(Integer failedDispatches) {
        this.failedDispatches = failedDispatches;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public List<PayrollPaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<PayrollPaymentResponse> payments) {
        this.payments = payments;
    }
}
