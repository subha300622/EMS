package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;


public class MyScheduleDashboardResponse {

    private EmployeeInfo employee;
    private TodaySummary todaySummary;
    private UpcomingSummary upcomingSummary;
    @Schema(example = "string")
    private String lastUpdatedAt;

    public static class EmployeeInfo {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "Software Engineer")
        private String designation;
        @Schema(example = "Engineering")
        private String department;
        @Schema(example = "string")
        private String managerName;

        // Getters and Setters
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getManagerName() { return managerName; }
        public void setManagerName(String managerName) { this.managerName = managerName; }
    }

    public static class TodaySummary {
        @Schema(example = "string")
        private String date;
        @Schema(example = "string")
        private String shift;
        @Schema(example = "string")
        private String workingHours;
        @Schema(example = "ACTIVE")
        private String attendanceStatus;
        @Schema(example = "1")
        private Integer meetingsToday;
        @Schema(example = "1")
        private Integer tasksToday;

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getShift() { return shift; }
        public void setShift(String shift) { this.shift = shift; }
        public String getWorkingHours() { return workingHours; }
        public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public void setAttendanceStatus(String status) { this.attendanceStatus = status; }
        public Integer getMeetingsToday() { return meetingsToday; }
        public void setMeetingsToday(Integer meetingsToday) { this.meetingsToday = meetingsToday; }
        public Integer getTasksToday() { return tasksToday; }
        public void setTasksToday(Integer tasksToday) { this.tasksToday = tasksToday; }
    }

    public static class UpcomingSummary {
        @Schema(example = "1")
        private Integer upcomingMeetings;
        @Schema(example = "1")
        private Integer upcomingLeaves;
        @Schema(example = "1")
        private Integer pendingChangeRequests;

        // Getters and Setters
        public Integer getUpcomingMeetings() { return upcomingMeetings; }
        public void setUpcomingMeetings(Integer upcomingMeetings) { this.upcomingMeetings = upcomingMeetings; }
        public Integer getUpcomingLeaves() { return upcomingLeaves; }
        public void setUpcomingLeaves(Integer upcomingLeaves) { this.upcomingLeaves = upcomingLeaves; }
        public Integer getPendingChangeRequests() { return pendingChangeRequests; }
        public void setPendingChangeRequests(Integer pendingChangeRequests) { this.pendingChangeRequests = pendingChangeRequests; }
    }

    // Getters and Setters
    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }
    public TodaySummary getTodaySummary() { return todaySummary; }
    public void setTodaySummary(TodaySummary todaySummary) { this.todaySummary = todaySummary; }
    public UpcomingSummary getUpcomingSummary() { return upcomingSummary; }
    public void setUpcomingSummary(UpcomingSummary upcomingSummary) { this.upcomingSummary = upcomingSummary; }
    public String getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
