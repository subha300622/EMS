package com.example.ems.holiday.dto;

import com.example.ems.holiday.entity.HolidayStatus;
import java.time.LocalDate;

public class HolidayResponseDto {

    private String holidayId;
    private String name;
    private LocalDate holidayDate;
    private String description;
    private HolidayStatus status;

    public HolidayResponseDto() {}

    public HolidayResponseDto(String holidayId, String name, LocalDate holidayDate, String description, HolidayStatus status) {
        this.holidayId = holidayId;
        this.name = name;
        this.holidayDate = holidayDate;
        this.description = description;
        this.status = status;
    }

    public String getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(String holidayId) {
        this.holidayId = holidayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HolidayStatus getStatus() {
        return status;
    }

    public void setStatus(HolidayStatus status) {
        this.status = status;
    }
}
