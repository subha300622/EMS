package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TrainingSessionRequest {

    @NotNull(message = "Course ID is required")
    @Schema(example = "1")
    private Long courseId;

    @NotBlank(message = "Trainer name is required")
    @Schema(example = "string")
    private String trainerName;

    @NotNull(message = "Schedule date is required")
    @Schema(example = "2026-06-19")
    private LocalDate scheduleDate;

    @Schema(example = "string")
    private String startTime;
    @Schema(example = "string")
    private String endTime;
    @Schema(example = "Bangalore")
    private String location;

    @Positive(message = "Capacity must be positive")
    @Schema(example = "1")
    private Integer capacity;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
