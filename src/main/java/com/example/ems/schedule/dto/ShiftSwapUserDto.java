package com.example.ems.schedule.dto;

public class ShiftSwapUserDto {
    private Long employeeId;
    private String name;
    private String designation;
    private String department;

    public ShiftSwapUserDto() {}

    public ShiftSwapUserDto(Long employeeId, String name, String designation, String department) {
        this.employeeId = employeeId;
        this.name = name;
        this.designation = designation;
        this.department = department;
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
}
