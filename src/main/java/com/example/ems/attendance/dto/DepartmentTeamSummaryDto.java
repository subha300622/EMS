package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department Team Attendance Summary")
public class DepartmentTeamSummaryDto {

    @Schema(description = "Team ID", example = "12")
    private Long teamId;

    @Schema(description = "Team name", example = "Backend Core Team")
    private String teamName;

    @Schema(description = "Team code", example = "BE-CORE")
    private String teamCode;

    @Schema(description = "Total employees in team", example = "8")
    private int totalEmployees;

    @Schema(description = "Present employees count", example = "7")
    private int presentCount;

    @Schema(description = "Absent employees count", example = "0")
    private int absentCount;

    @Schema(description = "Employees on leave count", example = "1")
    private int onLeaveCount;

    @Schema(description = "Late employees count", example = "1")
    private int lateCount;

    @Schema(description = "Not checked in count", example = "0")
    private int notCheckedInCount;

    public DepartmentTeamSummaryDto() {}

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getOnLeaveCount() {
        return onLeaveCount;
    }

    public void setOnLeaveCount(int onLeaveCount) {
        this.onLeaveCount = onLeaveCount;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    public int getNotCheckedInCount() {
        return notCheckedInCount;
    }

    public void setNotCheckedInCount(int notCheckedInCount) {
        this.notCheckedInCount = notCheckedInCount;
    }
}
