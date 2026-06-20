package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fnf_settlements")
public class FnfSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(precision = 12, scale = 2)
    private BigDecimal gratuity = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal noticePay = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal unpaidSalary = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FnfSettlementStatus status = FnfSettlementStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String workflowRemarks;

    private String approvedBy;
    private LocalDateTime approvedDate;

    private String processedBy;
    private LocalDateTime processedDate;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    private String paymentReference;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FnfSettlement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getNoticePay() { return noticePay; }
    public void setNoticePay(BigDecimal noticePay) { this.noticePay = noticePay; }

    public BigDecimal getUnpaidSalary() { return unpaidSalary; }
    public void setUnpaidSalary(BigDecimal unpaidSalary) { this.unpaidSalary = unpaidSalary; }

    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public FnfSettlementStatus getStatus() { return status; }
    public void setStatus(FnfSettlementStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getWorkflowRemarks() { return workflowRemarks; }
    public void setWorkflowRemarks(String workflowRemarks) { this.workflowRemarks = workflowRemarks; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }

    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
