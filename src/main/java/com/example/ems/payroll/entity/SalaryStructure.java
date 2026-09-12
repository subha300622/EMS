package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_structures", uniqueConstraints = {
    @UniqueConstraint(name = "uk_salary_structures_org_code_version", columnNames = {"organization_id", "code", "version"})
})
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 10)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_frequency", nullable = false, length = 50)
    private PayFrequency payFrequency = PayFrequency.MONTHLY;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SalaryStructureStatus status = SalaryStructureStatus.DRAFT;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(name = "updated_by", length = 150)
    private String updatedBy;

    // --- Legacy fields preserved for backward compatibility until Batch 5 ---
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(precision = 38, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 38, scale = 2)
    private BigDecimal hra;

    @Column(precision = 38, scale = 2)
    private BigDecimal allowances;

    public SalaryStructure() {}

    public SalaryStructure(Long employeeId, BigDecimal basicSalary, BigDecimal hra, BigDecimal allowances) {
        this.employeeId = employeeId;
        this.basicSalary = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        this.hra = hra != null ? hra : BigDecimal.ZERO;
        this.allowances = allowances != null ? allowances : BigDecimal.ZERO;
        this.organizationId = 1L;
        this.name = "Legacy Structure - Employee " + employeeId;
        this.code = "LEGACY_EMP_" + employeeId;
        this.status = SalaryStructureStatus.ACTIVE;
        this.version = 1;
    }

    public SalaryStructure(Long organizationId, String name, String code, String description, String currency, PayFrequency payFrequency, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.organizationId = organizationId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.currency = currency != null ? currency : "INR";
        this.payFrequency = payFrequency != null ? payFrequency : PayFrequency.MONTHLY;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = SalaryStructureStatus.DRAFT;
        this.version = 1;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.currency == null) {
            this.currency = "INR";
        }
        if (this.payFrequency == null) {
            this.payFrequency = PayFrequency.MONTHLY;
        }
        if (this.status == null) {
            this.status = SalaryStructureStatus.DRAFT;
        }
        if (this.version == null) {
            this.version = 1;
        }
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase();
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayFrequency getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(PayFrequency payFrequency) {
        this.payFrequency = payFrequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public SalaryStructureStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryStructureStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }
}
