package com.example.ems.attendance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CheckInRequest {
    @Schema(example = "string")
    private String notes;

    public CheckInRequest() {}

    public CheckInRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
