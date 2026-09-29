package com.example.ems.attendance.dto;

import com.example.ems.attendance.entity.AttendanceEarlyExitStatus;
import com.example.ems.attendance.entity.AttendanceLateStatus;
import com.example.ems.attendance.entity.AttendanceStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceDaySummaryDto {

    private Long attendanceId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private AttendanceStatus status;

    // Actual unchanged timestamps (Audit evidence)
    private Instant checkInTime;
    private Instant checkOutTime;
    private LocalTime punchInTime;
    private LocalTime punchOutTime;

    // Shift info
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;

    // Calculation results
    private Integer lateByMinutes;
    private Integer earlyByMinutes;
    private AttendanceLateStatus lateStatus;
    private AttendanceEarlyExitStatus earlyExitStatus;
    private Integer graceMinutes;
    private Integer permissionMinutes;
    private Integer payableMinutes;
    private Integer totalWorkingMinutes;
    private Integer totalBreakMinutes;
    private Integer overtimeMinutes;

    public AttendanceDaySummaryDto() {}

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

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }

    public Instant getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Instant checkOutTime) { this.checkOutTime = checkOutTime; }

    public LocalTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalTime punchInTime) { this.punchInTime = punchInTime; }

    public LocalTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalTime punchOutTime) { this.punchOutTime = punchOutTime; }

    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public void setShiftStartTime(LocalTime shiftStartTime) { this.shiftStartTime = shiftStartTime; }

    public LocalTime getShiftEndTime() { return shiftEndTime; }
    public void setShiftEndTime(LocalTime shiftEndTime) { this.shiftEndTime = shiftEndTime; }

    public Integer getLateByMinutes() { return lateByMinutes; }
    public void setLateByMinutes(Integer lateByMinutes) { this.lateByMinutes = lateByMinutes; }

    public Integer getEarlyByMinutes() { return earlyByMinutes; }
    public void setEarlyByMinutes(Integer earlyByMinutes) { this.earlyByMinutes = earlyByMinutes; }

    public AttendanceLateStatus getLateStatus() { return lateStatus; }
    public void setLateStatus(AttendanceLateStatus lateStatus) { this.lateStatus = lateStatus; }

    public AttendanceEarlyExitStatus getEarlyExitStatus() { return earlyExitStatus; }
    public void setEarlyExitStatus(AttendanceEarlyExitStatus earlyExitStatus) { this.earlyExitStatus = earlyExitStatus; }

    public Integer getGraceMinutes() { return graceMinutes; }
    public void setGraceMinutes(Integer graceMinutes) { this.graceMinutes = graceMinutes; }

    public Integer getPermissionMinutes() { return permissionMinutes; }
    public void setPermissionMinutes(Integer permissionMinutes) { this.permissionMinutes = permissionMinutes; }

    public Integer getPayableMinutes() { return payableMinutes; }
    public void setPayableMinutes(Integer payableMinutes) { this.payableMinutes = payableMinutes; }

    public Integer getTotalWorkingMinutes() { return totalWorkingMinutes; }
    public void setTotalWorkingMinutes(Integer totalWorkingMinutes) { this.totalWorkingMinutes = totalWorkingMinutes; }

    public Integer getTotalBreakMinutes() { return totalBreakMinutes; }
    public void setTotalBreakMinutes(Integer totalBreakMinutes) { this.totalBreakMinutes = totalBreakMinutes; }

    public Integer getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Integer overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }
}
