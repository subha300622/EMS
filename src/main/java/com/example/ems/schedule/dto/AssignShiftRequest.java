package com.example.ems.schedule.dto;

import com.example.ems.schedule.entity.ShiftType;
import java.time.LocalDate;

public class AssignShiftRequest {
    private Long employeeId;
    private LocalDate date;
    private ShiftType shiftType;

    public AssignShiftRequest() {}

    public AssignShiftRequest(Long employeeId, LocalDate date, ShiftType shiftType) {
        this.employeeId = employeeId;
        this.date = date;
        this.shiftType = shiftType;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }
}
