package com.example.ems.schedule.dto;

import java.util.List;

public class OvertimeSummaryDto {

    private double totalOvertimeHours; // 42.0
    private List<OvertimeMonitorDto> employees;

    public OvertimeSummaryDto() {}

    public OvertimeSummaryDto(double totalOvertimeHours, List<OvertimeMonitorDto> employees) {
        this.totalOvertimeHours = totalOvertimeHours;
        this.employees = employees;
    }

    public double getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(double totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public List<OvertimeMonitorDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<OvertimeMonitorDto> employees) {
        this.employees = employees;
    }
}
