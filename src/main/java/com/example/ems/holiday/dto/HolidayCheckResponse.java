package com.example.ems.holiday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Holiday check result response")
public class HolidayCheckResponse {

    @Schema(description = "Queried date (YYYY-MM-DD)", example = "2026-12-25")
    private LocalDate date;

    @Schema(description = "Whether the queried date is an active holiday", example = "true")
    private boolean isHoliday;

    @Schema(description = "Unique Holiday Identifier if date is a holiday", example = "HOL-2026-001")
    private String holidayId;

    @Schema(description = "Name of the holiday if date is a holiday", example = "Christmas Day")
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
