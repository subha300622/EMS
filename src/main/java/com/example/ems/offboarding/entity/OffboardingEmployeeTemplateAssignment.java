package com.example.ems.offboarding.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offboarding_employee_template_assignments", indexes = {
    @Index(name = "idx_offboarding_emp_tpl_org_emp", columnList = "organization_id, employee_id"),
    @Index(name = "idx_offboarding_emp_tpl_org_tpl", columnList = "organization_id, template_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_offboarding_emp_tpl_assign", columnNames = {"organization_id", "employee_id", "exit_type"})
})
public class OffboardingEmployeeTemplateAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    private OffboardingTemplate template;

    @Column(name = "exit_type", nullable = false, length = 50)
    private String exitType = "ALL";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OffboardingEmployeeTemplateAssignment() {}

    public OffboardingEmployeeTemplateAssignment(Long organizationId, Employee employee, OffboardingTemplate template, String exitType) {
        this.organizationId = organizationId;
        this.employee = employee;
        this.template = template;
        this.exitType = exitType != null && !exitType.trim().isEmpty() ? exitType.trim().toUpperCase() : "ALL";
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

    public OffboardingTemplate getTemplate() {
        return template;
    }

    public void setTemplate(OffboardingTemplate template) {
        this.template = template;
    }

    public String getExitType() {
        return exitType;
    }

    public void setExitType(String exitType) {
        this.exitType = exitType != null && !exitType.trim().isEmpty() ? exitType.trim().toUpperCase() : "ALL";
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
