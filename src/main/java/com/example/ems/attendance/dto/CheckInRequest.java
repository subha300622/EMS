package com.example.ems.attendance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CheckInRequest {
    @Schema(example = "string")
    private String notes;

    @Schema(example = "OFFICE")
    private String attendanceType;

    @Schema(example = "Bangalore Office")
    private String location;

    public CheckInRequest() {}

    public CheckInRequest(String notes) {
        this.notes = notes;
    }

    public CheckInRequest(String notes, String attendanceType, String location) {
        this.notes = notes;
        this.attendanceType = attendanceType;
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(String attendanceType) {
        this.attendanceType = attendanceType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
