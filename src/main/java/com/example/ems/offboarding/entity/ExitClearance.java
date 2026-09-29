package com.example.ems.offboarding.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "exit_clearances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_exit_clearance_department", columnNames = {"exit_id", "department"})
        },
        indexes = {
                @Index(name = "idx_exit_clearances_org_exit", columnList = "organization_id, exit_id"),
                @Index(name = "idx_exit_clearances_assigned", columnList = "assigned_to_id, status")
        })
public class ExitClearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exit_id", nullable = false)
    @JsonIgnore
    private EmployeeExit exit;

    @Column(nullable = false, length = 50)
    private String department; // IT, ADMIN, FINANCE, MANAGER

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee assignedTo;

    @Column(name = "clearance_reason", nullable = false, columnDefinition = "TEXT")
    private String clearanceReason;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, COMPLETED, REJECTED, HOLD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleared_by_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee clearedBy;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clearance_data", columnDefinition = "jsonb")
    private String clearanceData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ExitClearance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public EmployeeExit getExit() { return exit; }
    public void setExit(EmployeeExit exit) { this.exit = exit; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Employee getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Employee assignedTo) { this.assignedTo = assignedTo; }

    public String getClearanceReason() { return clearanceReason; }
    public void setClearanceReason(String clearanceReason) { this.clearanceReason = clearanceReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getClearedBy() { return clearedBy; }
    public void setClearedBy(Employee clearedBy) { this.clearedBy = clearedBy; }

    public LocalDateTime getClearedAt() { return clearedAt; }
    public void setClearedAt(LocalDateTime clearedAt) { this.clearedAt = clearedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getClearanceData() { return clearanceData; }
    public void setClearanceData(String clearanceData) { this.clearanceData = clearanceData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
