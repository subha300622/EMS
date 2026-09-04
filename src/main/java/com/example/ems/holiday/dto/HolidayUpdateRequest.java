package com.example.ems.holiday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class HolidayUpdateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "holidayDate is required")
    private LocalDate holidayDate;

    private String description;

    public HolidayUpdateRequest() {}

    public HolidayUpdateRequest(String name, LocalDate holidayDate, String description) {
        this.name = name;
        this.holidayDate = holidayDate;
        this.description = description;
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
}
