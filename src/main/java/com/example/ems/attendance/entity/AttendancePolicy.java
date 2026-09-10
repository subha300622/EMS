package com.example.ems.attendance.entity;

import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_policies", indexes = {
    @Index(name = "idx_att_policy_org_status", columnList = "organization_id, status")
})
public class AttendancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "office_start_time", nullable = false)
    private LocalTime officeStartTime;

    @Column(name = "office_end_time", nullable = false)
    private LocalTime officeEndTime;

    @Column(name = "grace_period_minutes", nullable = false)
    private Integer gracePeriodMinutes = 15;

    @Column(name = "minimum_working_minutes", nullable = false)
    private Integer minimumWorkingMinutes = 480;

    @Column(name = "half_day_threshold", nullable = false)
    private Integer halfDayThreshold = 240;

    @Column(name = "late_threshold", nullable = false)
    private Integer lateThreshold = 15;

    @Column(name = "early_checkout_threshold", nullable = false)
    private Integer earlyCheckoutThreshold = 15;

    @Column(name = "maximum_break_minutes", nullable = false)
    private Integer maximumBreakMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendancePolicyStatus status = AttendancePolicyStatus.DRAFT;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AttendancePolicy() {}

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.gracePeriodMinutes == null) this.gracePeriodMinutes = 15;
        if (this.minimumWorkingMinutes == null) this.minimumWorkingMinutes = 480;
        if (this.halfDayThreshold == null) this.halfDayThreshold = 240;
        if (this.lateThreshold == null) this.lateThreshold = 15;
        if (this.earlyCheckoutThreshold == null) this.earlyCheckoutThreshold = 15;
        if (this.maximumBreakMinutes == null) this.maximumBreakMinutes = 60;
        if (this.status == null) this.status = AttendancePolicyStatus.DRAFT;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getOfficeStartTime() {
        return officeStartTime;
    }

    public void setOfficeStartTime(LocalTime officeStartTime) {
        this.officeStartTime = officeStartTime;
    }

    public LocalTime getOfficeEndTime() {
        return officeEndTime;
    }

    public void setOfficeEndTime(LocalTime officeEndTime) {
        this.officeEndTime = officeEndTime;
    }

    public Integer getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public void setGracePeriodMinutes(Integer gracePeriodMinutes) {
        this.gracePeriodMinutes = gracePeriodMinutes;
    }

    public Integer getMinimumWorkingMinutes() {
        return minimumWorkingMinutes;
    }

    public void setMinimumWorkingMinutes(Integer minimumWorkingMinutes) {
        this.minimumWorkingMinutes = minimumWorkingMinutes;
    }

    public Integer getHalfDayThreshold() {
        return halfDayThreshold;
    }

    public void setHalfDayThreshold(Integer halfDayThreshold) {
        this.halfDayThreshold = halfDayThreshold;
    }

    public Integer getLateThreshold() {
        return lateThreshold;
    }

    public void setLateThreshold(Integer lateThreshold) {
        this.lateThreshold = lateThreshold;
    }

    public Integer getEarlyCheckoutThreshold() {
        return earlyCheckoutThreshold;
    }

    public void setEarlyCheckoutThreshold(Integer earlyCheckoutThreshold) {
        this.earlyCheckoutThreshold = earlyCheckoutThreshold;
    }

    public Integer getMaximumBreakMinutes() {
        return maximumBreakMinutes;
    }

    public void setMaximumBreakMinutes(Integer maximumBreakMinutes) {
        this.maximumBreakMinutes = maximumBreakMinutes;
    }

    public AttendancePolicyStatus getStatus() {
        return status;
    }

    public void setStatus(AttendancePolicyStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
