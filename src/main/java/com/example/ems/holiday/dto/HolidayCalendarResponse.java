package com.example.ems.holiday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Holiday calendar for a specific calendar year")
public class HolidayCalendarResponse {

    @Schema(description = "Calendar year", example = "2026")
    private int year;

    @Schema(description = "List of scheduled holidays in the year")
    private List<HolidayCalendarItem> holidays;

    public HolidayCalendarResponse() {}

    public HolidayCalendarResponse(int year, List<HolidayCalendarItem> holidays) {
        this.year = year;
        this.holidays = holidays;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<HolidayCalendarItem> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<HolidayCalendarItem> holidays) {
        this.holidays = holidays;
    }

    @Schema(description = "Holiday calendar item entry")
    public static class HolidayCalendarItem {
        @Schema(description = "Unique Holiday Identifier", example = "HOL-2026-001")
        private String holidayId;

        @Schema(description = "Date of the holiday (YYYY-MM-DD)", example = "2026-12-25")
        private LocalDate date;

        @Schema(description = "Name of the holiday", example = "Christmas Day")
        private String name;

        public HolidayCalendarItem() {}

        public HolidayCalendarItem(String holidayId, LocalDate date, String name) {
            this.holidayId = holidayId;
            this.date = date;
            this.name = name;
        }

        public String getHolidayId() {
            return holidayId;
        }

        public void setHolidayId(String holidayId) {
            this.holidayId = holidayId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
