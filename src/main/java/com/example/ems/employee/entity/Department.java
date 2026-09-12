package com.example.ems.employee.entity;

import jakarta.persistence.*;
import com.example.ems.organization.entity.Organization;
import java.math.BigDecimal;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String description;

    private Long parentDepartmentId;
    private Long managerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Organization organization;

    private BigDecimal budget = BigDecimal.ZERO;
    private String status = "ACTIVE";
    private String costCenter;
    private BigDecimal utilizedBudget = BigDecimal.ZERO;

    public Department() {}

    public Department(Long id, String name, String code, String description) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public Department(Long id, String name, String code, String description, Long parentDepartmentId, Long managerId, BigDecimal budget, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.parentDepartmentId = parentDepartmentId;
        this.managerId = managerId;
        this.budget = budget != null ? budget : BigDecimal.ZERO;
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

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
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

    public BigDecimal getUtilizedBudget() {
        return utilizedBudget;
    }

    public void setUtilizedBudget(BigDecimal utilizedBudget) {
        this.utilizedBudget = utilizedBudget;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
