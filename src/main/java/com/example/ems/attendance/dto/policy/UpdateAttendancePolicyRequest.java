package com.example.ems.attendance.dto.policy;

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
}
