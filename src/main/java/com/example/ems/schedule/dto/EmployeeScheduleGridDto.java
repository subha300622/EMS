package com.example.ems.schedule.dto;

import java.util.List;

public class EmployeeScheduleGridDto {
    private Long employeeId;
    private String name;
    private String designation;
    private String department;
    private String avatar;
    private List<DailyShiftDto> shifts;

    public EmployeeScheduleGridDto() {}

    public EmployeeScheduleGridDto(Long employeeId, String name, String designation, String department, String avatar, List<DailyShiftDto> shifts) {
        this.employeeId = employeeId;
        this.name = name;
        this.designation = designation;
        this.department = department;
        this.avatar = avatar;
        this.shifts = shifts;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<DailyShiftDto> getShifts() {
        return shifts;
    }

    public void setShifts(List<DailyShiftDto> shifts) {
        this.shifts = shifts;
    }
}
