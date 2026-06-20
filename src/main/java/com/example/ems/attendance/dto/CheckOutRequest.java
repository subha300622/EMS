package com.example.ems.attendance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CheckOutRequest {
    @Schema(example = "string")
    private String notes;

    public CheckOutRequest() {}

    public CheckOutRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
