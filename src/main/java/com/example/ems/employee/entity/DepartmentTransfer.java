package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_transfers")
public class DepartmentTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    private Long fromDepartmentId;

    @Column(nullable = false)
    private Long toDepartmentId;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private String remarks;

    private LocalDateTime transferDate = LocalDateTime.now();

    public DepartmentTransfer() {}

    public DepartmentTransfer(Long employeeId, Long fromDepartmentId, Long toDepartmentId, LocalDate effectiveDate, String remarks) {
        this.employeeId = employeeId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId = toDepartmentId;
        this.effectiveDate = effectiveDate;
        this.remarks = remarks;
        this.transferDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }
}
