package com.example.ems.schedule.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AvailabilityRequest {

    private List<AvailabilitySlot> availability;

    public static class AvailabilitySlot {
        @Schema(example = "string")
        private String dayOfWeek;
        @Schema(example = "string")
        private String availableFrom;
        @Schema(example = "string")
        private String availableTo;

        // Getters and Setters
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public String getAvailableFrom() { return availableFrom; }
        public void setAvailableFrom(String availableFrom) { this.availableFrom = availableFrom; }

        public String getAvailableTo() { return availableTo; }
        public void setAvailableTo(String availableTo) { this.availableTo = availableTo; }
    }

    public List<AvailabilitySlot> getAvailability() { return availability; }
    public void setAvailability(List<AvailabilitySlot> availability) { this.availability = availability; }
}
