package com.example.ems.attendance.dto.policy;

import com.example.ems.attendance.entity.ExceedGraceAction;
import com.example.ems.attendance.entity.GracePeriodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Request payload for updating an attendance policy")
public class UpdateAttendancePolicyRequest {

    @Size(max = 150, message = "Policy name cannot exceed 150 characters")
    @Schema(description = "Name of the attendance policy", example = "Updated Standard Policy")
    private String name;

    @Schema(description = "Office start time (HH:mm:ss)", example = "09:00:00")
    private LocalTime officeStartTime;

    @Schema(description = "Office end time (HH:mm:ss)", example = "18:00:00")
    private LocalTime officeEndTime;

    @Schema(description = "Grace period for check-in in minutes", example = "15")
    private Integer gracePeriodMinutes;

    @Schema(description = "Minimum working minutes for a full day", example = "480")
    private Integer minimumWorkingMinutes;

    @Schema(description = "Threshold for half-day working minutes", example = "240")
    private Integer halfDayThreshold;

    @Schema(description = "Late arrival threshold in minutes", example = "15")
    private Integer lateThreshold;

    @Schema(description = "Early checkout threshold in minutes before office end", example = "15")
    private Integer earlyCheckoutThreshold;

    @Schema(description = "Maximum allowed break minutes", example = "60")
    private Integer maximumBreakMinutes;

    @Schema(description = "Late grace minutes (automatic tolerance)", example = "10")
    private Integer lateGraceMinutes;

    @Schema(description = "Early exit grace minutes (automatic tolerance)", example = "10")
    private Integer earlyExitGraceMinutes;

    @Schema(description = "Number of grace occurrences allowed per period", example = "3")
    private Integer graceOccurrencesPerPeriod;

    @Schema(description = "Grace period type (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "MONTHLY")
    private GracePeriodType gracePeriodType;

    @Schema(description = "Allow late grace", example = "true")
    private Boolean allowLateGrace;

    @Schema(description = "Allow early exit grace", example = "true")
    private Boolean allowEarlyExitGrace;

    @Schema(description = "Action when grace limit is exceeded (MARK_LATE, MARK_HALF_DAY, MARK_ABSENT, DEDUCT_LEAVE)", example = "MARK_LATE")
    private ExceedGraceAction exceedGraceAction;

    @Schema(description = "Max allowed permissions per month", example = "4")
    private Integer maxMonthlyPermissions;

    @Schema(description = "Max permission minutes per day", example = "120")
    private Integer maxDailyPermissionMinutes;

    @Schema(description = "Max permission minutes per month", example = "480")
    private Integer maxMonthlyPermissionMinutes;

    public UpdateAttendancePolicyRequest() {}

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
}
