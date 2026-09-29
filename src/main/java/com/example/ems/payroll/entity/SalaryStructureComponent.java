package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_structure_components", uniqueConstraints = {
    @UniqueConstraint(name = "uk_salary_structure_component", columnNames = {"salary_structure_id", "salary_component_id"})
})
public class SalaryStructureComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "salary_component_id", nullable = false)
    private SalaryComponent salaryComponent;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 50)
    private CalculationType calculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_base_type", nullable = false, length = 50)
    private CalculationBaseType calculationBaseType = CalculationBaseType.NONE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "calculation_base_component_id")
    private SalaryComponent calculationBaseComponent;

    @Column(name = "fixed_amount", precision = 38, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 10, scale = 4)
    private BigDecimal percentage;

    @Column(columnDefinition = "TEXT")
    private String formula;

    @Column(name = "calculation_order", nullable = false)
    private Integer calculationOrder = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SalaryStructureComponent() {}

    public SalaryStructureComponent(SalaryStructure salaryStructure, SalaryComponent salaryComponent, CalculationType calculationType, CalculationBaseType calculationBaseType, SalaryComponent calculationBaseComponent, BigDecimal fixedAmount, BigDecimal percentage, String formula, Integer calculationOrder) {
        this.salaryStructure = salaryStructure;
        this.salaryComponent = salaryComponent;
        this.calculationType = calculationType;
        this.calculationBaseType = calculationBaseType != null ? calculationBaseType : CalculationBaseType.NONE;
        this.calculationBaseComponent = calculationBaseComponent;
        this.fixedAmount = fixedAmount;
        this.percentage = percentage;
        this.formula = formula;
        this.calculationOrder = calculationOrder != null ? calculationOrder : 1;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.calculationBaseType == null) {
            this.calculationBaseType = CalculationBaseType.NONE;
        }
        if (this.calculationOrder == null) {
            this.calculationOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SalaryStructure getSalaryStructure() {
        return salaryStructure;
    }

    public void setSalaryStructure(SalaryStructure salaryStructure) {
        this.salaryStructure = salaryStructure;
    }

    public SalaryComponent getSalaryComponent() {
        return salaryComponent;
    }

    public void setSalaryComponent(SalaryComponent salaryComponent) {
        this.salaryComponent = salaryComponent;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public CalculationBaseType getCalculationBaseType() {
        return calculationBaseType;
    }

    public void setCalculationBaseType(CalculationBaseType calculationBaseType) {
        this.calculationBaseType = calculationBaseType;
    }

    public SalaryComponent getCalculationBaseComponent() {
        return calculationBaseComponent;
    }

    public void setCalculationBaseComponent(SalaryComponent calculationBaseComponent) {
        this.calculationBaseComponent = calculationBaseComponent;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Integer getCalculationOrder() {
        return calculationOrder;
    }

    public void setCalculationOrder(Integer calculationOrder) {
        this.calculationOrder = calculationOrder;
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
