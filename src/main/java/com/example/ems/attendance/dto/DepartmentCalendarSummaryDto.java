package com.example.ems.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentCalendarSummaryDto {
    private Long departmentId;
    private Long managerId;
    private String month;
    private TotalsDto totals;
    private TrendDto trend;

    public DepartmentCalendarSummaryDto(Long departmentId, Long managerId, String month, TotalsDto totals, TrendDto trend) {
        this.departmentId = departmentId;
        this.managerId = managerId;
        this.month = month;
        this.totals = totals;
        this.trend = trend;
    }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public TotalsDto getTotals() { return totals; }
    public void setTotals(TotalsDto totals) { this.totals = totals; }

    public TrendDto getTrend() { return trend; }
    public void setTrend(TrendDto trend) { this.trend = trend; }

    public static class TotalsDto {
        private int workingDays;
        private double avgAttendance;
        private int totalEmployees;

        public TotalsDto(int workingDays, double avgAttendance, int totalEmployees) {
            this.workingDays = workingDays;
            this.avgAttendance = avgAttendance;
            this.totalEmployees = totalEmployees;
        }

        public int getWorkingDays() { return workingDays; }
        public void setWorkingDays(int workingDays) { this.workingDays = workingDays; }

        public double getAvgAttendance() { return avgAttendance; }
        public void setAvgAttendance(double avgAttendance) { this.avgAttendance = avgAttendance; }

        public int getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }
    }

    public static class TrendDto {
        private String bestDay;
        private String worstDay;

        public TrendDto(String bestDay, String worstDay) {
            this.bestDay = bestDay;
            this.worstDay = worstDay;
        }

        public String getBestDay() { return bestDay; }
        public void setBestDay(String bestDay) { this.bestDay = bestDay; }

        public String getWorstDay() { return worstDay; }
        public void setWorstDay(String worstDay) { this.worstDay = worstDay; }
    }
}
