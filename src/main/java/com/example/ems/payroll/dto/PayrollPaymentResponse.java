package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.PaymentProviderType;
import com.example.ems.payroll.entity.PayrollPayment;
import com.example.ems.payroll.entity.PayrollPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollPaymentResponse {

    private Long id;
    private Long organizationId;
    private Long payrollRunId;
    private Long payrollEmployeeId;
    private Long employeeId;
    private String idempotencyKey;
    private String payoutId;
    private BigDecimal amount;
    private String currency;
    private PayrollPaymentStatus status;
    private String failureReason;
    private String utr;
    private PaymentProviderType provider;
    private String mode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PayrollPaymentResponse() {}

    public static PayrollPaymentResponse fromEntity(PayrollPayment p) {
        if (p == null) return null;
        PayrollPaymentResponse res = new PayrollPaymentResponse();
        res.setId(p.getId());
        res.setOrganizationId(p.getOrganizationId());
        res.setPayrollRunId(p.getPayrollRunId());
        res.setPayrollEmployeeId(p.getPayrollEmployeeId());
        res.setEmployeeId(p.getEmployeeId());
        res.setIdempotencyKey(p.getIdempotencyKey());
        res.setPayoutId(p.getPayoutId());
        res.setAmount(p.getAmount());
        res.setCurrency(p.getCurrency());
        res.setStatus(p.getStatus());
        res.setFailureReason(p.getFailureReason());
        res.setUtr(p.getUtr());
        res.setProvider(p.getProvider());
        res.setMode(p.getMode());
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());
        return res;
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

    public Long getPayrollRunId() {
        return payrollRunId;
    }

    public void setPayrollRunId(Long payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public Long getPayrollEmployeeId() {
        return payrollEmployeeId;
    }

    public void setPayrollEmployeeId(Long payrollEmployeeId) {
        this.payrollEmployeeId = payrollEmployeeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(String payoutId) {
        this.payoutId = payoutId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayrollPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollPaymentStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getUtr() {
        return utr;
    }

    public void setUtr(String utr) {
        this.utr = utr;
    }

    public PaymentProviderType getProvider() {
        return provider;
    }

    public void setProvider(PaymentProviderType provider) {
        this.provider = provider;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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
