package com.example.ems.leave.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "year"})
})
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private Double totalEntitlement = 0.0;

    @Column(nullable = false)
    private Double usedBalance = 0.0;

    @Column(nullable = false)
    private Double pendingBalance = 0.0;

    @Column(nullable = false)
    private Integer year = LocalDateTime.now().getYear();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public LeaveBalance() {}

    public Double getAvailableBalance() {
        return totalEntitlement - usedBalance - pendingBalance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Double getTotalEntitlement() { return totalEntitlement; }
    public void setTotalEntitlement(Double totalEntitlement) { this.totalEntitlement = totalEntitlement; }

    public Double getUsedBalance() { return usedBalance; }
    public void setUsedBalance(Double usedBalance) { this.usedBalance = usedBalance; }

    public Double getPendingBalance() { return pendingBalance; }
    public void setPendingBalance(Double pendingBalance) { this.pendingBalance = pendingBalance; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
