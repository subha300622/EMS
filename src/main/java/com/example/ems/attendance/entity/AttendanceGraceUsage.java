package com.example.ems.attendance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "attendance_grace_usage", indexes = {
    @Index(name = "idx_att_grace_usage_org_emp_date", columnList = "organization_id, employee_id, attendance_date"),
    @Index(name = "idx_att_grace_usage_period_lookup", columnList = "organization_id, employee_id, grace_type, attendance_date")
})
public class AttendanceGraceUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "grace_type", nullable = false, length = 30)
    private String graceType; // 'LATE_ARRIVAL', 'EARLY_EXIT'

    @Column(name = "grace_minutes_used", nullable = false)
    private Integer graceMinutesUsed = 0;

    @Column(name = "within_grace", nullable = false)
    private Boolean withinGrace = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private AttendancePolicy policy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AttendanceGraceUsage() {}

    public AttendanceGraceUsage(Organization organization, Employee employee, LocalDate attendanceDate,
                                String graceType, Integer graceMinutesUsed, Boolean withinGrace, AttendancePolicy policy) {
        this.organization = organization;
        this.employee = employee;
        this.attendanceDate = attendanceDate;
        this.graceType = graceType;
        this.graceMinutesUsed = graceMinutesUsed != null ? graceMinutesUsed : 0;
        this.withinGrace = withinGrace != null ? withinGrace : true;
        this.policy = policy;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getGraceType() {
        return graceType;
    }

    public void setGraceType(String graceType) {
        this.graceType = graceType;
    }

    public Integer getGraceMinutesUsed() {
        return graceMinutesUsed;
    }

    public void setGraceMinutesUsed(Integer graceMinutesUsed) {
        this.graceMinutesUsed = graceMinutesUsed;
    }

    public Boolean getWithinGrace() {
        return withinGrace;
    }

    public void setWithinGrace(Boolean withinGrace) {
        this.withinGrace = withinGrace;
    }

    public AttendancePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(AttendancePolicy policy) {
        this.policy = policy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
