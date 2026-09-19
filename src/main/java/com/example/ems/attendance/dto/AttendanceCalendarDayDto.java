package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Single Day Attendance Detail on the Calendar")
public class AttendanceCalendarDayDto {

    @Schema(description = "Calendar date", example = "2026-09-10")
    private LocalDate date;

    @Schema(description = "Day of week", example = "THURSDAY")
    private String dayOfWeek;

    @Schema(description = "Computed attendance status (HOLIDAY, WEEK_OFF, ON_LEAVE, PRESENT, HALF_DAY, ABSENT, NOT_CHECKED_IN)", example = "PRESENT")
    private String status;

    @Schema(description = "Server check-in timestamp")
    private Instant checkInTime;

    @Schema(description = "Server check-out timestamp")
    private Instant checkOutTime;

    @Schema(description = "Total working minutes", example = "495")
    private Integer totalWorkingMinutes;

    @Schema(description = "Total break minutes", example = "45")
    private Integer totalBreakMinutes;

    @Schema(description = "Whether the employee checked in late", example = "false")
    private Boolean isLate = false;

    @Schema(description = "Late duration HH:mm", example = "00:00")
    private String lateBy = "00:00";

    @Schema(description = "Holiday name if status is HOLIDAY", example = "Labor Day")
    private String holidayName;

    @Schema(description = "Leave type name if status is ON_LEAVE", example = "CASUAL_LEAVE")
    private String leaveType;

    @Schema(description = "List of breaks taken during the day")
    private List<AttendanceBreakDto> breaks = new ArrayList<>();

    public AttendanceCalendarDayDto() {}

    public AttendanceCalendarDayDto(LocalDate date, String dayOfWeek, String status) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Instant getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Instant checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Integer getTotalWorkingMinutes() {
        return totalWorkingMinutes;
    }

    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) {
        this.totalWorkingMinutes = totalWorkingMinutes;
    }

    public Integer getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(Integer totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public String getLateBy() {
        return lateBy;
    }

    public void setLateBy(String lateBy) {
        this.lateBy = lateBy;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public List<AttendanceBreakDto> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<AttendanceBreakDto> breaks) {
        this.breaks = breaks;
    }
}
