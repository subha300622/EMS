package com.example.ems.asset.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "my_asset_assignments")
public class MyAssetAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private MyAsset asset;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate assignedDate = LocalDate.now();

    private LocalDate expectedReturnDate;

    private LocalDate returnedDate;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, RETURNED, TRANSFERRED

    private String remarks;

    public MyAssetAssignment() {}

    public MyAssetAssignment(MyAsset asset, Employee employee, LocalDate assignedDate, LocalDate expectedReturnDate, String status, String remarks) {
        this.asset = asset;
        this.employee = employee;
        this.assignedDate = assignedDate != null ? assignedDate : LocalDate.now();
        this.expectedReturnDate = expectedReturnDate;
        this.status = status != null ? status : "ACTIVE";
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyAsset getAsset() {
        return asset;
    }

    public void setAsset(MyAsset asset) {
        this.asset = asset;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
