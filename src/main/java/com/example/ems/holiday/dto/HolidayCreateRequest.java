package com.example.ems.holiday.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Request payload for creating an organization holiday")
public class HolidayCreateRequest {

    @Schema(description = "Name of the holiday", example = "Christmas Day", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "name is required")
    private String name;

    @Schema(description = "Date of the holiday (YYYY-MM-DD)", example = "2026-12-25", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "holidayDate is required")
    private LocalDate holidayDate;

    @Schema(description = "Optional description or notes for the holiday", example = "Public holiday celebration")
    private String description;

    public HolidayCreateRequest() {}

    public HolidayCreateRequest(String name, LocalDate holidayDate, String description) {
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
