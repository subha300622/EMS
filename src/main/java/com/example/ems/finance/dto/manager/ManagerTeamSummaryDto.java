package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Manager Team Summary metrics")
public class ManagerTeamSummaryDto implements Serializable {

    @Schema(description = "Total number of employees reporting to manager", example = "24")
    private Integer totalEmployees;

    @Schema(description = "Employees present today", example = "22")
    private Integer presentToday;

    @Schema(description = "Employees on leave today", example = "2")
    private Integer onLeaveToday;

    @Schema(description = "Attendance percentage for today", example = "91.67")
    private Double attendancePercentage;

    public ManagerTeamSummaryDto() {}

    public ManagerTeamSummaryDto(Integer totalEmployees, Integer presentToday, Integer onLeaveToday, Double attendancePercentage) {
        this.totalEmployees = totalEmployees;
        this.presentToday = presentToday;
        this.onLeaveToday = onLeaveToday;
        this.attendancePercentage = attendancePercentage;
    }

    public Integer getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(Integer totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public Integer getPresentToday() {
        return presentToday;
    }

    public void setPresentToday(Integer presentToday) {
        this.presentToday = presentToday;
    }

    public Integer getOnLeaveToday() {
        return onLeaveToday;
    }

    public void setOnLeaveToday(Integer onLeaveToday) {
        this.onLeaveToday = onLeaveToday;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}
