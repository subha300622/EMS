package com.example.ems.attendance.dto;

public class CheckOutRequest {
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
