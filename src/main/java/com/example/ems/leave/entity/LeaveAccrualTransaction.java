package com.example.ems.leave.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_accrual_transactions")
public class LeaveAccrualTransaction {

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
    private Double accruedAmount;

    private String period; // e.g. "2026-08" or "2026-Q3"

    private LocalDateTime accruedAt = LocalDateTime.now();

    public LeaveAccrualTransaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Double getAccruedAmount() { return accruedAmount; }
    public void setAccruedAmount(Double accruedAmount) { this.accruedAmount = accruedAmount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDateTime getAccruedAt() { return accruedAt; }
    public void setAccruedAt(LocalDateTime accruedAt) { this.accruedAt = accruedAt; }
}
