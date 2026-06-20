package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.training.entity.TrainingSession;

import java.time.LocalDate;

public class TrainingSessionResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long courseId;
    @Schema(example = "Project Deliverables")
    private String courseTitle;
    @Schema(example = "string")
    private String trainerName;
    @Schema(example = "2026-06-19")
    private LocalDate scheduleDate;
    @Schema(example = "string")
    private String startTime;
    @Schema(example = "string")
    private String endTime;
    @Schema(example = "Bangalore")
    private String location;
    @Schema(example = "1")
    private Integer capacity;
    @Schema(example = "1")
    private Integer enrolledCount;

    public TrainingSessionResponse() {}

    public TrainingSessionResponse(TrainingSession session) {
        this.id = session.getId();
        this.trainerName = session.getTrainerName();
        this.scheduleDate = session.getScheduleDate();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.location = session.getLocation();
        this.capacity = session.getCapacity();

        if (session.getCourse() != null) {
            this.courseId = session.getCourse().getId();
            this.courseTitle = session.getCourse().getTitle();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

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

    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
}
