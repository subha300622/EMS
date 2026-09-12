package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_salary_component_values",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_emp_sal_comp_val", columnNames = {"salary_assignment_id", "salary_component_id"})
        },
        indexes = {
                @Index(name = "idx_emp_sal_comp_val_assign", columnList = "salary_assignment_id"),
                @Index(name = "idx_emp_sal_comp_val_comp", columnList = "salary_component_id")
        })
public class EmployeeSalaryComponentValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_assignment_id", nullable = false)
    private EmployeeSalaryAssignment salaryAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_component_id", nullable = false)
    private SalaryComponent salaryComponent;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_type", nullable = false, length = 30)
    private ComponentOverrideType overrideType = ComponentOverrideType.FIXED_AMOUNT;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public EmployeeSalaryComponentValue() {}

    public EmployeeSalaryComponentValue(EmployeeSalaryAssignment salaryAssignment, SalaryComponent salaryComponent,
                                        BigDecimal amount, BigDecimal percentage, ComponentOverrideType overrideType) {
        this.salaryAssignment = salaryAssignment;
        this.salaryComponent = salaryComponent;
        this.amount = amount;
        this.percentage = percentage;
        this.overrideType = overrideType != null ? overrideType : ComponentOverrideType.FIXED_AMOUNT;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmployeeSalaryAssignment getSalaryAssignment() {
        return salaryAssignment;
    }

    public void setSalaryAssignment(EmployeeSalaryAssignment salaryAssignment) {
        this.salaryAssignment = salaryAssignment;
    }

    public SalaryComponent getSalaryComponent() {
        return salaryComponent;
    }

    public void setSalaryComponent(SalaryComponent salaryComponent) {
        this.salaryComponent = salaryComponent;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public ComponentOverrideType getOverrideType() {
        return overrideType;
    }

    public void setOverrideType(ComponentOverrideType overrideType) {
        this.overrideType = overrideType;
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
