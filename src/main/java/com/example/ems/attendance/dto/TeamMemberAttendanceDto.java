package com.example.ems.attendance.dto;

import java.util.List;

public class TeamMemberAttendanceDto {
    private Long employeeId;
    private String name;
    private String designation;
    private String workMode;
    private List<AttendanceRecordDto> attendance;

    public TeamMemberAttendanceDto() {}

    public TeamMemberAttendanceDto(Long employeeId, String name, String designation, String workMode, List<AttendanceRecordDto> attendance) {
        this.employeeId = employeeId;
        this.name = name;
        this.designation = designation;
        this.workMode = workMode;
        this.attendance = attendance;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public List<AttendanceRecordDto> getAttendance() { return attendance; }
    public void setAttendance(List<AttendanceRecordDto> attendance) { this.attendance = attendance; }

    public static class AttendanceRecordDto {
        private String date;
        private String status;
        private String checkIn;
        private String checkOut;
        private String workingHours;

        public AttendanceRecordDto() {}

        public AttendanceRecordDto(String date, String status, String checkIn, String checkOut, String workingHours) {
            this.date = date;
            this.status = status;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.workingHours = workingHours;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }

        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }

        public String getWorkingHours() { return workingHours; }
        public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    }
}
