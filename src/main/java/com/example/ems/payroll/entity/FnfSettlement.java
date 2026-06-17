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

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, PROCESSED

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
