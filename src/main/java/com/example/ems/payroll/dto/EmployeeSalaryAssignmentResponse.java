package com.example.ems.payroll.dto;

import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.SalaryAssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeSalaryAssignmentResponse {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long salaryStructureId;
    private String salaryStructureName;
    private String salaryStructureCode;
    private Integer salaryStructureVersion;
    private String currency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private SalaryAssignmentStatus status;
    private String reason;
    private List<EmployeeSalaryComponentValueResponse> componentValues = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeSalaryAssignmentResponse() {}

    public static EmployeeSalaryAssignmentResponse fromEntity(EmployeeSalaryAssignment entity) {
        if (entity == null) {
            return null;
        }
        EmployeeSalaryAssignmentResponse dto = new EmployeeSalaryAssignmentResponse();
        dto.setId(entity.getId());
        dto.setOrganizationId(entity.getOrganizationId());

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
            dto.setEmployeeName(entity.getEmployee().getFullName());
            dto.setEmployeeCode(entity.getEmployee().getEmployeeId());
        }

        if (entity.getSalaryStructure() != null) {
            dto.setSalaryStructureId(entity.getSalaryStructure().getId());
            dto.setSalaryStructureName(entity.getSalaryStructure().getName());
            dto.setSalaryStructureCode(entity.getSalaryStructure().getCode());
            dto.setSalaryStructureVersion(entity.getSalaryStructure().getVersion());
            dto.setCurrency(entity.getSalaryStructure().getCurrency());
        }

        dto.setEffectiveFrom(entity.getEffectiveFrom());
        dto.setEffectiveTo(entity.getEffectiveTo());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getComponentValues() != null) {
            dto.setComponentValues(entity.getComponentValues().stream()
                    .map(EmployeeSalaryComponentValueResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
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

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public Long getSalaryStructureId() {
        return salaryStructureId;
    }

    public void setSalaryStructureId(Long salaryStructureId) {
        this.salaryStructureId = salaryStructureId;
    }

    public String getSalaryStructureName() {
        return salaryStructureName;
    }

    public void setSalaryStructureName(String salaryStructureName) {
        this.salaryStructureName = salaryStructureName;
    }

    public String getSalaryStructureCode() {
        return salaryStructureCode;
    }

    public void setSalaryStructureCode(String salaryStructureCode) {
        this.salaryStructureCode = salaryStructureCode;
    }

    public Integer getSalaryStructureVersion() {
        return salaryStructureVersion;
    }

    public void setSalaryStructureVersion(Integer salaryStructureVersion) {
        this.salaryStructureVersion = salaryStructureVersion;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public List<EmployeeSalaryComponentValueResponse> getComponentValues() {
        return componentValues;
    }

    public void setComponentValues(List<EmployeeSalaryComponentValueResponse> componentValues) {
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
}
