package com.example.ems.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Monthly Attendance Calendar Response for Authenticated Employee")
public class MonthlyAttendanceCalendarResponse {

    @Schema(description = "Calendar Year", example = "2026")
    private int year;

    @Schema(description = "Calendar Month (1-12)", example = "9")
    private int month;

    @Schema(description = "Total days in month", example = "30")
    private int totalDays;

    @Schema(description = "Total working days (excluding week offs and holidays)", example = "22")
    private int workingDays;

    @Schema(description = "Total present days", example = "18")
    private int presentDays;

    @Schema(description = "Total absent days", example = "1")
    private int absentDays;

    @Schema(description = "Total half days", example = "1")
    private int halfDays;

    @Schema(description = "Total approved leave days", example = "2")
    private int leaveDays;

    @Schema(description = "Total holiday days", example = "1")
    private int holidayDays;

    @Schema(description = "Total weekend days / week offs", example = "8")
    private int weekOffDays;

    @Schema(description = "Total days not checked in (today/future)", example = "0")
    private int notCheckedInDays;

    @Schema(description = "Total working minutes accumulated in the month", example = "8820")
    private int totalWorkingMinutes;

    @Schema(description = "Total break minutes accumulated in the month", example = "900")
    private int totalBreakMinutes;

    @Schema(description = "Average working minutes per working day attended", example = "490.0")
    private double averageWorkingMinutes;

    @Schema(description = "Daily calendar breakdown")
    private List<AttendanceCalendarDayDto> days = new ArrayList<>();

    public MonthlyAttendanceCalendarResponse() {}

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(int absentDays) {
        this.absentDays = absentDays;
    }

    public int getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(int halfDays) {
        this.halfDays = halfDays;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(int leaveDays) {
        this.leaveDays = leaveDays;
    }

    public int getHolidayDays() {
        return holidayDays;
    }

    public void setHolidayDays(int holidayDays) {
        this.holidayDays = holidayDays;
    }

    public int getWeekOffDays() {
        return weekOffDays;
    }

    public void setWeekOffDays(int weekOffDays) {
        this.weekOffDays = weekOffDays;
    }

    public int getNotCheckedInDays() {
        return notCheckedInDays;
    }

    public void setNotCheckedInDays(int notCheckedInDays) {
        this.notCheckedInDays = notCheckedInDays;
    }

    public int getTotalWorkingMinutes() {
        return totalWorkingMinutes;
    }

    public void setTotalWorkingMinutes(int totalWorkingMinutes) {
        this.totalWorkingMinutes = totalWorkingMinutes;
    }

    public int getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(int totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public double getAverageWorkingMinutes() {
        return averageWorkingMinutes;
    }

    public void setAverageWorkingMinutes(double averageWorkingMinutes) {
        this.averageWorkingMinutes = averageWorkingMinutes;
    }

    public List<AttendanceCalendarDayDto> getDays() {
        return days;
    }

    public void setDays(List<AttendanceCalendarDayDto> days) {
        this.days = days;
    }
}
