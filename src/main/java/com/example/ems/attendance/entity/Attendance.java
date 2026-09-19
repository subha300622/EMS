package com.example.ems.attendance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(name = "unique_employee_date", columnNames = {"employee_id", "date"}),
    @UniqueConstraint(name = "uk_attendance_org_employee_date", columnNames = {"organization_id", "employee_id", "date"})
}, indexes = {
    @Index(name = "idx_attendance_org_emp_date", columnList = "organization_id, employee_id, date")
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    private Organization organization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Convert(converter = AttendanceStatusConverter.class)
    private AttendanceStatus status;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Column(name = "total_break_minutes")
    private Integer totalBreakMinutes = 0;

    @Column(name = "total_working_minutes")
    private Integer totalWorkingMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_status", nullable = false, length = 30)
    private AttendanceLateStatus lateStatus = AttendanceLateStatus.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "early_exit_status", nullable = false, length = 30)
    private AttendanceEarlyExitStatus earlyExitStatus = AttendanceEarlyExitStatus.NONE;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes = 0;

    @Column(name = "permission_minutes", nullable = false)
    private Integer permissionMinutes = 0;

    @Column(name = "payable_minutes", nullable = false)
    private Integer payableMinutes = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("breakStartTime ASC")
    private List<AttendanceBreak> breaks = new ArrayList<>();

    // Legacy fields preserved for reporting & backwards compatibility
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
    private Instant serverTime;

    @Column(name = "is_late")
    private Boolean isLate = false;

    @Column(name = "late_by")
    private String lateBy;

    @Column(name = "late_by_minutes")
    private Integer lateByMinutes = 0;

    @Column(name = "is_early_checkout")
    private Boolean isEarlyCheckout = false;

    @Column(name = "early_by")
    private String earlyBy = "00:00";

    @Column(name = "early_by_minutes")
    private Integer earlyByMinutes = 0;

    @Column(name = "is_half_day")
    private Boolean isHalfDay = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.lateStatus == null) this.lateStatus = AttendanceLateStatus.NONE;
        if (this.earlyExitStatus == null) this.earlyExitStatus = AttendanceEarlyExitStatus.NONE;
        if (this.graceMinutes == null) this.graceMinutes = 0;
        if (this.permissionMinutes == null) this.permissionMinutes = 0;
        if (this.payableMinutes == null) this.payableMinutes = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Helper methods for breaks
    public void addBreak(AttendanceBreak attendanceBreak) {
        if (attendanceBreak != null) {
            this.breaks.add(attendanceBreak);
            attendanceBreak.setAttendance(this);
            if (this.organization != null && attendanceBreak.getOrganizationId() == null) {
                attendanceBreak.setOrganizationId(this.organization.getId());
            }
        }
    }

    public Optional<AttendanceBreak> getActiveBreak() {
        return breaks.stream().filter(AttendanceBreak::isActive).findFirst();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public AttendanceStatus getAttendanceStatus() {
        return status;
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
                } else if ("HOLIDAY".equalsIgnoreCase(status)) {
                    this.status = AttendanceStatus.HOLIDAY;
                } else {
                    this.status = AttendanceStatus.PRESENT;
                }
            }
        }
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public Integer getTotalBreakMinutes() { return totalBreakMinutes; }
    public void setTotalBreakMinutes(Integer totalBreakMinutes) { this.totalBreakMinutes = totalBreakMinutes; }

    public Integer getTotalWorkingMinutes() { return totalWorkingMinutes; }
    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) { this.totalWorkingMinutes = totalWorkingMinutes; }

    public AttendanceLateStatus getLateStatus() { return lateStatus; }
    public void setLateStatus(AttendanceLateStatus lateStatus) { this.lateStatus = lateStatus; }

    public AttendanceEarlyExitStatus getEarlyExitStatus() { return earlyExitStatus; }
    public void setEarlyExitStatus(AttendanceEarlyExitStatus earlyExitStatus) { this.earlyExitStatus = earlyExitStatus; }

    public Integer getGraceMinutes() { return graceMinutes != null ? graceMinutes : 0; }
    public void setGraceMinutes(Integer graceMinutes) { this.graceMinutes = graceMinutes; }

    public Integer getPermissionMinutes() { return permissionMinutes != null ? permissionMinutes : 0; }
    public void setPermissionMinutes(Integer permissionMinutes) { this.permissionMinutes = permissionMinutes; }

    public Integer getPayableMinutes() { return payableMinutes != null ? payableMinutes : 0; }
    public void setPayableMinutes(Integer payableMinutes) { this.payableMinutes = payableMinutes; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<AttendanceBreak> getBreaks() { return breaks; }
    public void setBreaks(List<AttendanceBreak> breaks) { this.breaks = breaks; }

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

    public String location() { return location; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getServerTime() { return serverTime; }
    public void setServerTime(Instant serverTime) { this.serverTime = serverTime; }

    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }

    public String getLateBy() { return lateBy; }
    public void setLateBy(String lateBy) { this.lateBy = lateBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Transient
    @JsonProperty("regularizationStatus")
    private String regularizationStatus;

    public String getRegularizationStatus() { return regularizationStatus; }
    public void setRegularizationStatus(String regularizationStatus) { this.regularizationStatus = regularizationStatus; }

    @Transient
    @JsonProperty("workingHours")
    public String getWorkingHours() {
        if (checkInTime != null && checkOutTime != null) {
            long totalMins = totalWorkingMinutes != null ? totalWorkingMinutes : Duration.between(checkInTime, checkOutTime).toMinutes();
            long hours = totalMins / 60;
            long minutes = totalMins % 60;
            return String.format("%02d:%02d", hours, minutes);
        }
        if (punchInTime == null || punchOutTime == null) {
            return null;
        }
        Duration duration = Duration.between(punchInTime, punchOutTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    public Integer getLateByMinutes() { return lateByMinutes != null ? lateByMinutes : 0; }
    public void setLateByMinutes(Integer lateByMinutes) { this.lateByMinutes = lateByMinutes; }

    public Boolean getIsEarlyCheckout() { return isEarlyCheckout != null ? isEarlyCheckout : false; }
    public void setIsEarlyCheckout(Boolean isEarlyCheckout) { this.isEarlyCheckout = isEarlyCheckout; }

    public String getEarlyBy() { return earlyBy; }
    public void setEarlyBy(String earlyBy) { this.earlyBy = earlyBy; }

    public Integer getEarlyByMinutes() { return earlyByMinutes != null ? earlyByMinutes : 0; }
    public void setEarlyByMinutes(Integer earlyByMinutes) { this.earlyByMinutes = earlyByMinutes; }

    public Boolean getIsHalfDay() { return isHalfDay != null ? isHalfDay : false; }
    public void setIsHalfDay(Boolean isHalfDay) { this.isHalfDay = isHalfDay; }

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
