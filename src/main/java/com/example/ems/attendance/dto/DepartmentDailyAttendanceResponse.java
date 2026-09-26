package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Department Daily Attendance Summary and Team Breakdown Response")
public class DepartmentDailyAttendanceResponse {

    @Schema(description = "Department ID", example = "5")
    private Long departmentId;

    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Department code", example = "ENG")
    private String departmentCode;

    @Schema(description = "Target date", example = "2026-09-10")
    private LocalDate date;

    @Schema(description = "Total department employees", example = "40")
    private int totalEmployees;

    @Schema(description = "Total present count", example = "35")
    private int presentCount;

    @Schema(description = "Total absent count", example = "2")
    private int absentCount;

    @Schema(description = "Total on leave count", example = "2")
    private int onLeaveCount;

    @Schema(description = "Total late count", example = "3")
    private int lateCount;

    @Schema(description = "Total not checked in count", example = "1")
    private int notCheckedInCount;

    @Schema(description = "Team-wise attendance summaries")
    private List<DepartmentTeamSummaryDto> teams = new ArrayList<>();

    @Schema(description = "Department members detailed attendance records")
    private List<DepartmentMemberDailyAttendanceDto> members = new ArrayList<>();

    public DepartmentDailyAttendanceResponse() {}

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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

    public List<DepartmentTeamSummaryDto> getTeams() {
        return teams;
    }

    public void setTeams(List<DepartmentTeamSummaryDto> teams) {
        this.teams = teams;
    }

    public List<DepartmentMemberDailyAttendanceDto> getMembers() {
        return members;
    }

    public void setMembers(List<DepartmentMemberDailyAttendanceDto> members) {
        this.members = members;
    }
}
