package com.example.ems.attendance.dto.late;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Late Attendance Report Item")
public class LateAttendanceReportDto {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private Instant checkInTime;
    private LocalTime expectedStartTime;
    private Integer gracePeriodMinutes;
    private Integer lateByMinutes;
    private String lateBy;
    private String department;
    private String team;
    private String status;

    public LateAttendanceReportDto() {}

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public LocalTime getExpectedStartTime() { return expectedStartTime; }
    public void setExpectedStartTime(LocalTime expectedStartTime) { this.expectedStartTime = expectedStartTime; }

    public Integer getGracePeriodMinutes() { return gracePeriodMinutes; }
    public void setGracePeriodMinutes(Integer gracePeriodMinutes) { this.gracePeriodMinutes = gracePeriodMinutes; }

    public Integer getLateByMinutes() { return lateByMinutes; }
    public void setLateByMinutes(Integer lateByMinutes) { this.lateByMinutes = lateByMinutes; }

    public String getLateBy() { return lateBy; }
    public void setLateBy(String lateBy) { this.lateBy = lateBy; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
