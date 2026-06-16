package com.example.ems.offboarding.dto;

import java.time.LocalDate;

public class MyExitDashboardResponse {

    private EmployeeInfo employee;
    private ExitRequestInfo exitRequest;
    private ProgressInfo progress;

    public MyExitDashboardResponse() {}

    public MyExitDashboardResponse(EmployeeInfo employee, ExitRequestInfo exitRequest, ProgressInfo progress) {
        this.employee = employee;
        this.exitRequest = exitRequest;
        this.progress = progress;
    }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public ExitRequestInfo getExitRequest() { return exitRequest; }
    public void setExitRequest(ExitRequestInfo exitRequest) { this.exitRequest = exitRequest; }

    public ProgressInfo getProgress() { return progress; }
    public void setProgress(ProgressInfo progress) { this.progress = progress; }

    public static class EmployeeInfo {
        private Long id;
        private String employeeCode;
        private String name;
        private String department;
        private String designation;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String employeeCode, String name, String department, String designation) {
            this.id = id;
            this.employeeCode = employeeCode;
            this.name = name;
            this.department = department;
            this.designation = designation;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
    }

    public static class ExitRequestInfo {
        private Long id;
        private String status;
        private LocalDate resignationDate;
        private LocalDate lastWorkingDay;
        private Long daysRemaining;
        private String currentStage;

        public ExitRequestInfo() {}

        public ExitRequestInfo(Long id, String status, LocalDate resignationDate, LocalDate lastWorkingDay, Long daysRemaining, String currentStage) {
            this.id = id;
            this.status = status;
            this.resignationDate = resignationDate;
            this.lastWorkingDay = lastWorkingDay;
            this.daysRemaining = daysRemaining;
            this.currentStage = currentStage;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDate getResignationDate() { return resignationDate; }
        public void setResignationDate(LocalDate resignationDate) { this.resignationDate = resignationDate; }

        public LocalDate getLastWorkingDay() { return lastWorkingDay; }
        public void setLastWorkingDay(LocalDate lastWorkingDay) { this.lastWorkingDay = lastWorkingDay; }

        public Long getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(Long daysRemaining) { this.daysRemaining = daysRemaining; }

        public String getCurrentStage() { return currentStage; }
        public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
    }

    public static class ProgressInfo {
        private int completedTasks;
        private int totalTasks;
        private int completionPercentage;

        public ProgressInfo() {}

        public ProgressInfo(int completedTasks, int totalTasks, int completionPercentage) {
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
            this.completionPercentage = completionPercentage;
        }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    }
}
