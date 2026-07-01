package com.example.ems.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    private Long parentDepartmentId;
    private Long managerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private com.example.ems.organization.entity.Organization organization;

    private java.math.BigDecimal budget = java.math.BigDecimal.ZERO;
    private String status = "ACTIVE";
    private String costCenter;
    private java.math.BigDecimal utilizedBudget = java.math.BigDecimal.ZERO;

    public Department() {}

    public Department(Long id, String name, String code, String description) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public Department(Long id, String name, String code, String description, Long parentDepartmentId, Long managerId, java.math.BigDecimal budget, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.parentDepartmentId = parentDepartmentId;
        this.managerId = managerId;
        this.budget = budget != null ? budget : java.math.BigDecimal.ZERO;
        this.status = status != null ? status : "ACTIVE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(Long parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public java.math.BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(java.math.BigDecimal budget) {
        this.budget = budget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public java.math.BigDecimal getUtilizedBudget() {
        return utilizedBudget;
    }

    public void setUtilizedBudget(java.math.BigDecimal utilizedBudget) {
        this.utilizedBudget = utilizedBudget;
    }

    public com.example.ems.organization.entity.Organization getOrganization() {
        return organization;
    }

    public void setOrganization(com.example.ems.organization.entity.Organization organization) {
        this.organization = organization;
    }
}
