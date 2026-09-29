package com.example.ems.leave.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_leave_policies", uniqueConstraints = {
    @UniqueConstraint(name = "uk_emp_leave_policy", columnNames = {"employee_id", "leave_policy_id"})
})
public class EmployeeLeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"manager", "team", "hibernateLazyInitializer", "handler"})
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_policy_id", nullable = false)
    private LeavePolicy leavePolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    private Organization organization;

    private LocalDateTime assignedAt = LocalDateTime.now();

    private boolean active = true;

    public EmployeeLeavePolicy() {}

    public EmployeeLeavePolicy(Employee employee, LeavePolicy leavePolicy, Organization organization) {
        this.employee = employee;
        this.leavePolicy = leavePolicy;
        this.organization = organization;
        this.assignedAt = LocalDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LeavePolicy getLeavePolicy() { return leavePolicy; }
    public void setLeavePolicy(LeavePolicy leavePolicy) { this.leavePolicy = leavePolicy; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
