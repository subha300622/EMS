package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "payroll_payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_payment_idempotency", columnNames = {"idempotency_key"})
    }
)
public class PayrollPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "payroll_run_id", nullable = false)
    private Long payrollRunId;

    @Column(name = "payroll_employee_id", nullable = false)
    private Long payrollEmployeeId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "payout_id", length = 100)
    private String payoutId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayrollPaymentStatus status = PayrollPaymentStatus.PENDING;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "utr", length = 100)
    private String utr;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProviderType provider = PaymentProviderType.RAZORPAYX;

    @Column(name = "mode", nullable = false, length = 30)
    private String mode = "NEFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PayrollPayment() {}

    public PayrollPayment(Long organizationId, Long payrollRunId, Long payrollEmployeeId,
                          Long employeeId, String idempotencyKey, BigDecimal amount,
                          String currency, PaymentProviderType provider, String mode) {
        this.organizationId = organizationId;
        this.payrollRunId = payrollRunId;
        this.payrollEmployeeId = payrollEmployeeId;
        this.employeeId = employeeId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
        this.provider = provider != null ? provider : PaymentProviderType.RAZORPAYX;
        this.mode = mode != null ? mode : "NEFT";
        this.status = PayrollPaymentStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currency == null) this.currency = "INR";
        if (this.status == null) this.status = PayrollPaymentStatus.PENDING;
        if (this.provider == null) this.provider = PaymentProviderType.RAZORPAYX;
        if (this.mode == null) this.mode = "NEFT";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

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
