package com.example.ems.employee.dto;

import java.time.LocalDate;

public class DepartmentTransferRequest {

    private Long employeeId;
    private Long fromDepartmentId;
    private Long toDepartmentId;
    private LocalDate effectiveDate;
    private String remarks;

    public DepartmentTransferRequest() {}

    public DepartmentTransferRequest(Long employeeId, Long fromDepartmentId, Long toDepartmentId, LocalDate effectiveDate, String remarks) {
        this.employeeId = employeeId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId = toDepartmentId;
        this.effectiveDate = effectiveDate;
        this.remarks = remarks;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getFromDepartmentId() {
        return fromDepartmentId;
    }

    public void setFromDepartmentId(Long fromDepartmentId) {
        this.fromDepartmentId = fromDepartmentId;
    }

    public Long getToDepartmentId() {
        return toDepartmentId;
    }

    public void setToDepartmentId(Long toDepartmentId) {
        this.toDepartmentId = toDepartmentId;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
