package com.example.ems.attendance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "attendance_breaks", indexes = {
    @Index(name = "idx_attendance_breaks_att_id", columnList = "attendance_id"),
    @Index(name = "idx_attendance_breaks_org_id", columnList = "organization_id")
})
public class AttendanceBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    @JsonIgnore
    private Attendance attendance;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "break_start_time", nullable = false)
    private Instant breakStartTime;

    @Column(name = "break_end_time")
    private Instant breakEndTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public AttendanceBreak() {}

    public AttendanceBreak(Attendance attendance, Long organizationId, Instant breakStartTime) {
        this.attendance = attendance;
        this.organizationId = organizationId;
        this.breakStartTime = breakStartTime;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return breakEndTime == null;
    }

    public void closeBreak(Instant endTime) {
        if (endTime == null) {
            throw new IllegalArgumentException("Break end time cannot be null");
        }
        if (this.breakStartTime != null && endTime.isBefore(this.breakStartTime)) {
            throw new IllegalArgumentException("Break end time cannot be before break start time");
        }
        this.breakEndTime = endTime;
        if (this.breakStartTime != null) {
            long minutes = Duration.between(this.breakStartTime, endTime).toMinutes();
            this.durationMinutes = (int) Math.max(0, minutes);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Instant getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(Instant breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public Instant getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(Instant breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
