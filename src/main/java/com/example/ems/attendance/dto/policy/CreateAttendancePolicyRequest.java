package com.example.ems.attendance.dto.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Request payload for creating an attendance policy")
public class CreateAttendancePolicyRequest {

    @NotBlank(message = "Policy name is mandatory")
    @Size(max = 150, message = "Policy name cannot exceed 150 characters")
    @Schema(description = "Name of the attendance policy", example = "Standard Office Policy")
    private String name;

    @NotNull(message = "Office start time is mandatory")
    @Schema(description = "Office start time (HH:mm:ss)", example = "09:00:00")
    private LocalTime officeStartTime;

    @NotNull(message = "Office end time is mandatory")
    @Schema(description = "Office end time (HH:mm:ss)", example = "18:00:00")
    private LocalTime officeEndTime;

    @Schema(description = "Grace period for check-in in minutes", example = "15", defaultValue = "15")
    private Integer gracePeriodMinutes = 15;

    @Schema(description = "Minimum working minutes for a full day", example = "480", defaultValue = "480")
    private Integer minimumWorkingMinutes = 480;

    @Schema(description = "Threshold for half-day working minutes", example = "240", defaultValue = "240")
    private Integer halfDayThreshold = 240;

    @Schema(description = "Late arrival threshold in minutes", example = "15", defaultValue = "15")
    private Integer lateThreshold = 15;

    @Schema(description = "Early checkout threshold in minutes before office end", example = "15", defaultValue = "15")
    private Integer earlyCheckoutThreshold = 15;

    @Schema(description = "Maximum allowed break minutes", example = "60", defaultValue = "60")
    private Integer maximumBreakMinutes = 60;

    public CreateAttendancePolicyRequest() {}

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
