package com.example.ems.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class AttendanceStatsResponse {
    private int totalDays;
    private double attendancePercentage;
    private double absencePercentage;
    private int lateMarkCount;

    private Map<String, Double> statusDistribution;
    private Map<String, Integer> stabilityMetrics;
    private List<MonthlyTrend> monthlyTrends;
    private List<SystemAlert> systemAlerts;

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public double getAbsencePercentage() { return absencePercentage; }
    public void setAbsencePercentage(double absencePercentage) { this.absencePercentage = absencePercentage; }

    public int getLateMarkCount() { return lateMarkCount; }
    public void setLateMarkCount(int lateMarkCount) { this.lateMarkCount = lateMarkCount; }

    public Map<String, Double> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Double> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Integer> getStabilityMetrics() { return stabilityMetrics; }
    public void setStabilityMetrics(Map<String, Integer> stabilityMetrics) { this.stabilityMetrics = stabilityMetrics; }

    public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrend> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

    public List<SystemAlert> getSystemAlerts() { return systemAlerts; }
    public void setSystemAlerts(List<SystemAlert> systemAlerts) { this.systemAlerts = systemAlerts; }

    public static class MonthlyTrend {
        private String month;
        private double percentage;

        public MonthlyTrend(String month, double percentage) {
            this.month = month;
            this.percentage = percentage;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class SystemAlert {
        private String type;
        private String title;
        private String message;

        public SystemAlert(String type, String title, String message) {
            this.type = type;
            this.title = title;
            this.message = message;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
