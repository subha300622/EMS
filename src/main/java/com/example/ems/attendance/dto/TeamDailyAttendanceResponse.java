package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Team Daily Attendance Summary and Member Details Response")
public class TeamDailyAttendanceResponse {

    @Schema(description = "Team ID", example = "12")
    private Long teamId;

    @Schema(description = "Team name", example = "Backend Core Team")
    private String teamName;

    @Schema(description = "Team code", example = "BE-CORE")
    private String teamCode;

    @Schema(description = "Target date", example = "2026-09-10")
    private LocalDate date;

    @Schema(description = "Total team employees", example = "15")
    private int totalEmployees;

    @Schema(description = "Count of present members", example = "12")
    private int presentCount;

    @Schema(description = "Count of absent members", example = "1")
    private int absentCount;

    @Schema(description = "Count of members on leave", example = "1")
    private int onLeaveCount;

    @Schema(description = "Count of late members", example = "2")
    private int lateCount;

    @Schema(description = "Count of members who have not checked in yet", example = "1")
    private int notCheckedInCount;

    @Schema(description = "Detailed list of team members and their attendance")
    private List<TeamMemberDailyAttendanceDto> members = new ArrayList<>();

    public TeamDailyAttendanceResponse() {}

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public List<TeamMemberDailyAttendanceDto> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberDailyAttendanceDto> members) {
        this.members = members;
    }
}
