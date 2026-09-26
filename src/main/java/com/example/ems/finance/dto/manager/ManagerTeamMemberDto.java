package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Team member finance and workforce summary")
public class ManagerTeamMemberDto implements Serializable {

    @Schema(description = "Employee ID", example = "101")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "Rajan Kumar")
    private String name;

    @Schema(description = "Employee designation", example = "Senior Accountant")
    private String designation;

    @Schema(description = "Attendance percentage for current month", example = "96.0")
    private Double attendancePercentage;

    @Schema(description = "Remaining leave balance days", example = "8.0")
    private Double leaveBalance;

    @Schema(description = "Count of pending expense claims", example = "1")
    private Long pendingExpenses;

    public ManagerTeamMemberDto() {}

    public ManagerTeamMemberDto(Long employeeId, String name, String designation, Double attendancePercentage, Double leaveBalance, Long pendingExpenses) {
        this.employeeId = employeeId;
        this.name = name;
        this.designation = designation;
        this.attendancePercentage = attendancePercentage;
        this.leaveBalance = leaveBalance;
        this.pendingExpenses = pendingExpenses;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public Double getLeaveBalance() {
        return leaveBalance;
    }

    public void setLeaveBalance(Double leaveBalance) {
        this.leaveBalance = leaveBalance;
    }

    public Long getPendingExpenses() {
        return pendingExpenses;
    }

    public void setPendingExpenses(Long pendingExpenses) {
        this.pendingExpenses = pendingExpenses;
    }
}
