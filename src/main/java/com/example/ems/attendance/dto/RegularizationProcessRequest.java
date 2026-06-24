package com.example.ems.attendance.dto;

import java.time.LocalTime;

public class RegularizationProcessRequest {
    private LocalTime correctedPunchInTime;
    private LocalTime correctedPunchOutTime;
    private String managerNotes;

    public RegularizationProcessRequest() {}

    public RegularizationProcessRequest(LocalTime correctedPunchInTime, LocalTime correctedPunchOutTime, String managerNotes) {
        this.correctedPunchInTime = correctedPunchInTime;
        this.correctedPunchOutTime = correctedPunchOutTime;
        this.managerNotes = managerNotes;
    }

    public LocalTime getCorrectedPunchInTime() {
        return correctedPunchInTime;
    }

    public void setCorrectedPunchInTime(LocalTime correctedPunchInTime) {
        this.correctedPunchInTime = correctedPunchInTime;
    }

    public LocalTime getCorrectedPunchOutTime() {
        return correctedPunchOutTime;
    }

    public void setCorrectedPunchOutTime(LocalTime correctedPunchOutTime) {
        this.correctedPunchOutTime = correctedPunchOutTime;
    }

    public String getManagerNotes() {
        return managerNotes;
    }

    public void setManagerNotes(String managerNotes) {
        this.managerNotes = managerNotes;
    }
}
