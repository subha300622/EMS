package com.example.ems.performance.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PerformanceCycleRequest {

    @NotBlank(message = "Cycle name is required")
    @Schema(example = "string")
    private String name;

    @NotNull(message = "Start date is required")
    @Schema(example = "2026-06-19")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(example = "2026-06-19")
    private LocalDate endDate;

    @Schema(example = "ACTIVE")
    private String status; // ACTIVE, CLOSED

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
