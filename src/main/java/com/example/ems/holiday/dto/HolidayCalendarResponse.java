package com.example.ems.holiday.dto;

import java.time.LocalDate;
import java.util.List;

public class HolidayCalendarResponse {

    private int year;
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

    public static class HolidayCalendarItem {
        private String holidayId;
        private LocalDate date;
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
