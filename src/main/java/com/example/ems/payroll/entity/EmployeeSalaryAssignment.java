package com.example.ems.payroll.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_salary_assignments", indexes = {
        @Index(name = "idx_emp_sal_assign_org_emp", columnList = "organization_id, employee_id"),
        @Index(name = "idx_emp_sal_assign_org_emp_eff", columnList = "organization_id, employee_id, effective_from"),
        @Index(name = "idx_emp_sal_assign_org_status", columnList = "organization_id, status"),
        @Index(name = "idx_emp_sal_assign_struct", columnList = "salary_structure_id")
})
public class EmployeeSalaryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SalaryAssignmentStatus status = SalaryAssignmentStatus.ACTIVE;

    @Column(name = "reason", length = 255)
    private String reason;

    @OneToMany(mappedBy = "salaryAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeSalaryComponentValue> componentValues = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public EmployeeSalaryAssignment() {}

    public EmployeeSalaryAssignment(Long organizationId, Employee employee, SalaryStructure salaryStructure,
                                    LocalDate effectiveFrom, LocalDate effectiveTo,
                                    SalaryAssignmentStatus status, String reason) {
        this.organizationId = organizationId;
        this.employee = employee;
        this.salaryStructure = salaryStructure;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.status = status != null ? status : SalaryAssignmentStatus.ACTIVE;
        this.reason = reason;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public SalaryStructure getSalaryStructure() {
        return salaryStructure;
    }

    public void setSalaryStructure(SalaryStructure salaryStructure) {
        this.salaryStructure = salaryStructure;
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

    public SalaryAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryAssignmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<EmployeeSalaryComponentValue> getComponentValues() {
        return componentValues;
    }

    public void setComponentValues(List<EmployeeSalaryComponentValue> componentValues) {
        this.componentValues = componentValues;
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
}
