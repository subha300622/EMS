package com.example.ems.schedule.dto;


public class MyScheduleDashboardResponse {

    private EmployeeInfo employee;
    private TodaySummary todaySummary;
    private UpcomingSummary upcomingSummary;
    private String lastUpdatedAt;

    public static class EmployeeInfo {
        private Long employeeId;
        private String employeeCode;
        private String fullName;
        private String designation;
        private String department;
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
        private String date;
        private String shift;
        private String workingHours;
        private String attendanceStatus;
        private Integer meetingsToday;
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
        private Integer upcomingMeetings;
        private Integer upcomingLeaves;
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
