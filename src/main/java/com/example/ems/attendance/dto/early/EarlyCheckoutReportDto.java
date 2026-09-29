package com.example.ems.attendance.dto.early;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Early Checkout Report Item")
public class EarlyCheckoutReportDto {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private Instant checkOutTime;
    private LocalTime expectedEndTime;
    private Integer earlyCheckoutThresholdMinutes;
    private Integer earlyByMinutes;
    private String earlyBy;
    private String department;
    private String team;
    private String status;

    public EarlyCheckoutReportDto() {}

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

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public LocalTime getExpectedEndTime() { return expectedEndTime; }
    public void setExpectedEndTime(LocalTime expectedEndTime) { this.expectedEndTime = expectedEndTime; }

    public Integer getEarlyCheckoutThresholdMinutes() { return earlyCheckoutThresholdMinutes; }
    public void setEarlyCheckoutThresholdMinutes(Integer earlyCheckoutThresholdMinutes) { this.earlyCheckoutThresholdMinutes = earlyCheckoutThresholdMinutes; }

    public Integer getEarlyByMinutes() { return earlyByMinutes; }
    public void setEarlyByMinutes(Integer earlyByMinutes) { this.earlyByMinutes = earlyByMinutes; }

    public String getEarlyBy() { return earlyBy; }
    public void setEarlyBy(String earlyBy) { this.earlyBy = earlyBy; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
