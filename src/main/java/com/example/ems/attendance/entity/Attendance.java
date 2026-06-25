package com.example.ems.attendance.entity;

import com.example.ems.employee.entity.Employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(name = "unique_employee_date", columnNames = {"employee_id", "date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private LocalTime punchInTime;

    private LocalTime punchOutTime;

    private LocalTime originalPunchInTime;

    private LocalTime originalPunchOutTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "attendance_type")
    private String attendanceType;

    private String location;

    @Column(name = "server_time")
    private java.time.Instant serverTime;

    @Column(name = "is_late")
    private Boolean isLate = false;

    @Column(name = "late_by")
    private String lateBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        if (status == null) {
            this.status = null;
        } else {
            try {
                this.status = AttendanceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                if ("ON LEAVE".equalsIgnoreCase(status) || "LEAVE".equalsIgnoreCase(status)) {
                    this.status = AttendanceStatus.LEAVE;
                } else if ("HALF_DAY".equalsIgnoreCase(status) || "HALF DAY".equalsIgnoreCase(status)) {
                    this.status = AttendanceStatus.HALF_DAY;
                } else {
                    this.status = AttendanceStatus.PRESENT;
                }
            }
        }
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalTime punchInTime) { this.punchInTime = punchInTime; }

    public LocalTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalTime punchOutTime) { this.punchOutTime = punchOutTime; }

    public LocalTime getOriginalPunchInTime() { return originalPunchInTime; }
    public void setOriginalPunchInTime(LocalTime originalPunchInTime) { this.originalPunchInTime = originalPunchInTime; }

    public LocalTime getOriginalPunchOutTime() { return originalPunchOutTime; }
    public void setOriginalPunchOutTime(LocalTime originalPunchOutTime) { this.originalPunchOutTime = originalPunchOutTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getAttendanceType() { return attendanceType; }
    public void setAttendanceType(String attendanceType) { this.attendanceType = attendanceType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public java.time.Instant getServerTime() { return serverTime; }
    public void setServerTime(java.time.Instant serverTime) { this.serverTime = serverTime; }

    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }

    public String getLateBy() { return lateBy; }
    public void setLateBy(String lateBy) { this.lateBy = lateBy; }

    @Transient
    @JsonProperty("regularizationStatus")
    private String regularizationStatus;

    public String getRegularizationStatus() { return regularizationStatus; }
    public void setRegularizationStatus(String regularizationStatus) { this.regularizationStatus = regularizationStatus; }

    @Transient
    @JsonProperty("workingHours")
    public String getWorkingHours() {
        if (punchInTime == null || punchOutTime == null) {
            return null;
        }
        Duration duration = Duration.between(punchInTime, punchOutTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    @Transient
    @JsonProperty("overtime")
    public String getOvertime() {
        if (punchInTime == null || punchOutTime == null) {
            return "00:00";
        }
        Duration duration = Duration.between(punchInTime, punchOutTime);
        Duration standardDuration = Duration.ofHours(9);
        if (duration.compareTo(standardDuration) <= 0) {
            return "00:00";
        }
        Duration overtimeDuration = duration.minus(standardDuration);
        long hours = overtimeDuration.toHours();
        long minutes = overtimeDuration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
