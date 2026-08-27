package com.example.ems.holiday.dto;

import java.time.LocalDate;

public class HolidayCheckResponse {

    private LocalDate date;
    private boolean isHoliday;
    private String holidayId;
    private String holidayName;

    public HolidayCheckResponse() {}

    public HolidayCheckResponse(LocalDate date, boolean isHoliday, String holidayId, String holidayName) {
        this.date = date;
        this.isHoliday = isHoliday;
        this.holidayId = holidayId;
        this.holidayName = holidayName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public String getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(String holidayId) {
        this.holidayId = holidayId;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }
}
