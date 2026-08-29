package com.example.ems.training.dto;

import com.example.ems.training.entity.RecurrenceFrequency;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RecurrenceConfigRequest {

    @NotNull(message = "Recurrence frequency is required")
    private RecurrenceFrequency frequency;

    private Integer intervalVal = 1;
    private String daysOfWeek; // E.g., MONDAY,WEDNESDAY

    @NotNull(message = "Recurrence start date is required")
    private LocalDate startDate;

    @NotNull(message = "Recurrence end date is required")
    private LocalDate endDate;

    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }

    public Integer getIntervalVal() { return intervalVal; }
    public void setIntervalVal(Integer intervalVal) { this.intervalVal = intervalVal; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
