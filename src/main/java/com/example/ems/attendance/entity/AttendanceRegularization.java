package com.example.ems.attendance.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_regularizations")
public class AttendanceRegularization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime proposedPunchInTime;

    private LocalTime proposedPunchOutTime;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private String reason;

    private String managerNotes;

    public AttendanceRegularization() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getProposedPunchInTime() {
        return proposedPunchInTime;
    }

    public void setProposedPunchInTime(LocalTime proposedPunchInTime) {
        this.proposedPunchInTime = proposedPunchInTime;
    }

    public LocalTime getProposedPunchOutTime() {
        return proposedPunchOutTime;
    }

    public void setProposedPunchOutTime(LocalTime proposedPunchOutTime) {
        this.proposedPunchOutTime = proposedPunchOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getManagerNotes() {
        return managerNotes;
    }

    public void setManagerNotes(String managerNotes) {
        this.managerNotes = managerNotes;
    }
}
