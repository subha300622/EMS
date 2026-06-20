package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;

public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Schema(example = "string")
    private String name;

    @NotBlank(message = "Department code is required")
    @Schema(example = "EMP101")
    private String code;

    @Schema(example = "Detailed description of the item")
    private String description;

    private Long parentDepartmentId;
    private Long managerId;
    private java.math.BigDecimal budget;
    private String status = "ACTIVE";
    private String costCenter;
    private java.math.BigDecimal utilizedBudget;

    public DepartmentRequest() {}

    public DepartmentRequest(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public DepartmentRequest(String name, String code, String description, Long parentDepartmentId, Long managerId, java.math.BigDecimal budget, String status) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.parentDepartmentId = parentDepartmentId;
        this.managerId = managerId;
        this.budget = budget;
        this.status = status;
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
}
