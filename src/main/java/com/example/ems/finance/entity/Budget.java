package com.example.ems.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedFunds = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal utilizedFunds = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, DRAFT

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Budget() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getAllocatedFunds() {
        return allocatedFunds;
    }

    public void setAllocatedFunds(BigDecimal allocatedFunds) {
        this.allocatedFunds = allocatedFunds;
    }

    public BigDecimal getUtilizedFunds() {
        return utilizedFunds;
    }

    public void setUtilizedFunds(BigDecimal utilizedFunds) {
        this.utilizedFunds = utilizedFunds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
