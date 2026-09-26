package com.example.ems.attendance.dto.policy;

import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.entity.ExceedGraceAction;
import com.example.ems.attendance.entity.GracePeriodType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalTime;

@Schema(description = "Attendance Policy Details")
public class AttendancePolicyDto {

    private Long id;
    private String name;
    private LocalTime officeStartTime;
    private LocalTime officeEndTime;
    private Integer gracePeriodMinutes;
    private Integer minimumWorkingMinutes;
    private Integer halfDayThreshold;
    private Integer lateThreshold;
    private Integer earlyCheckoutThreshold;
    private Integer maximumBreakMinutes;

    private Integer lateGraceMinutes;
    private Integer earlyExitGraceMinutes;
    private Integer graceOccurrencesPerPeriod;
    private GracePeriodType gracePeriodType;
    private Boolean allowLateGrace;
    private Boolean allowEarlyExitGrace;
    private ExceedGraceAction exceedGraceAction;
    private Integer maxMonthlyPermissions;
    private Integer maxDailyPermissionMinutes;
    private Integer maxMonthlyPermissionMinutes;

    private AttendancePolicyStatus status;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public AttendancePolicyDto() {}

    public static AttendancePolicyDto fromEntity(AttendancePolicy policy) {
        if (policy == null) return null;
        AttendancePolicyDto dto = new AttendancePolicyDto();
        dto.setId(policy.getId());
        dto.setName(policy.getName());
        dto.setOfficeStartTime(policy.getOfficeStartTime());
        dto.setOfficeEndTime(policy.getOfficeEndTime());
        dto.setGracePeriodMinutes(policy.getGracePeriodMinutes());
        dto.setMinimumWorkingMinutes(policy.getMinimumWorkingMinutes());
        dto.setHalfDayThreshold(policy.getHalfDayThreshold());
        dto.setLateThreshold(policy.getLateThreshold());
        dto.setEarlyCheckoutThreshold(policy.getEarlyCheckoutThreshold());
        dto.setMaximumBreakMinutes(policy.getMaximumBreakMinutes());

        dto.setLateGraceMinutes(policy.getLateGraceMinutes());
        dto.setEarlyExitGraceMinutes(policy.getEarlyExitGraceMinutes());
        dto.setGraceOccurrencesPerPeriod(policy.getGraceOccurrencesPerPeriod());
        dto.setGracePeriodType(policy.getGracePeriodType());
        dto.setAllowLateGrace(policy.getAllowLateGrace());
        dto.setAllowEarlyExitGrace(policy.getAllowEarlyExitGrace());
        dto.setExceedGraceAction(policy.getExceedGraceAction());
        dto.setMaxMonthlyPermissions(policy.getMaxMonthlyPermissions());
        dto.setMaxDailyPermissionMinutes(policy.getMaxDailyPermissionMinutes());
        dto.setMaxMonthlyPermissionMinutes(policy.getMaxMonthlyPermissionMinutes());

        dto.setStatus(policy.getStatus());
        dto.setVersion(policy.getVersion());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalTime getOfficeStartTime() { return officeStartTime; }
    public void setOfficeStartTime(LocalTime officeStartTime) { this.officeStartTime = officeStartTime; }

    public LocalTime getOfficeEndTime() { return officeEndTime; }
    public void setOfficeEndTime(LocalTime officeEndTime) { this.officeEndTime = officeEndTime; }

    public Integer getGracePeriodMinutes() { return gracePeriodMinutes; }
    public void setGracePeriodMinutes(Integer gracePeriodMinutes) { this.gracePeriodMinutes = gracePeriodMinutes; }

    public Integer getMinimumWorkingMinutes() { return minimumWorkingMinutes; }
    public void setMinimumWorkingMinutes(Integer minimumWorkingMinutes) { this.minimumWorkingMinutes = minimumWorkingMinutes; }

    public Integer getHalfDayThreshold() { return halfDayThreshold; }
    public void setHalfDayThreshold(Integer halfDayThreshold) { this.halfDayThreshold = halfDayThreshold; }

    public Integer getLateThreshold() { return lateThreshold; }
    public void setLateThreshold(Integer lateThreshold) { this.lateThreshold = lateThreshold; }

    public Integer getEarlyCheckoutThreshold() { return earlyCheckoutThreshold; }
    public void setEarlyCheckoutThreshold(Integer earlyCheckoutThreshold) { this.earlyCheckoutThreshold = earlyCheckoutThreshold; }

    public Integer getMaximumBreakMinutes() { return maximumBreakMinutes; }
    public void setMaximumBreakMinutes(Integer maximumBreakMinutes) { this.maximumBreakMinutes = maximumBreakMinutes; }

    public Integer getLateGraceMinutes() { return lateGraceMinutes; }
    public void setLateGraceMinutes(Integer lateGraceMinutes) { this.lateGraceMinutes = lateGraceMinutes; }

    public Integer getEarlyExitGraceMinutes() { return earlyExitGraceMinutes; }
    public void setEarlyExitGraceMinutes(Integer earlyExitGraceMinutes) { this.earlyExitGraceMinutes = earlyExitGraceMinutes; }

    public Integer getGraceOccurrencesPerPeriod() { return graceOccurrencesPerPeriod; }
    public void setGraceOccurrencesPerPeriod(Integer graceOccurrencesPerPeriod) { this.graceOccurrencesPerPeriod = graceOccurrencesPerPeriod; }

    public GracePeriodType getGracePeriodType() { return gracePeriodType; }
    public void setGracePeriodType(GracePeriodType gracePeriodType) { this.gracePeriodType = gracePeriodType; }

    public Boolean getAllowLateGrace() { return allowLateGrace; }
    public void setAllowLateGrace(Boolean allowLateGrace) { this.allowLateGrace = allowLateGrace; }

    public Boolean getAllowEarlyExitGrace() { return allowEarlyExitGrace; }
    public void setAllowEarlyExitGrace(Boolean allowEarlyExitGrace) { this.allowEarlyExitGrace = allowEarlyExitGrace; }

    public ExceedGraceAction getExceedGraceAction() { return exceedGraceAction; }
    public void setExceedGraceAction(ExceedGraceAction exceedGraceAction) { this.exceedGraceAction = exceedGraceAction; }

    public Integer getMaxMonthlyPermissions() { return maxMonthlyPermissions; }
    public void setMaxMonthlyPermissions(Integer maxMonthlyPermissions) { this.maxMonthlyPermissions = maxMonthlyPermissions; }

    public Integer getMaxDailyPermissionMinutes() { return maxDailyPermissionMinutes; }
    public void setMaxDailyPermissionMinutes(Integer maxDailyPermissionMinutes) { this.maxDailyPermissionMinutes = maxDailyPermissionMinutes; }

    public Integer getMaxMonthlyPermissionMinutes() { return maxMonthlyPermissionMinutes; }
    public void setMaxMonthlyPermissionMinutes(Integer maxMonthlyPermissionMinutes) { this.maxMonthlyPermissionMinutes = maxMonthlyPermissionMinutes; }

    public AttendancePolicyStatus getStatus() { return status; }
    public void setStatus(AttendancePolicyStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
