package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TeamManagementDtos {

    @Schema(description = "Team member attendance entry")
    public record TeamAttendanceItemDto(
        @Schema(description = "Employee ID", example = "101")
        Long employeeId,
        @Schema(description = "Employee Full Name", example = "Jane Doe")
        String employeeName,
        @Schema(description = "Employee Email", example = "jane.doe@example.com")
        String employeeEmail,
        @Schema(description = "Attendance Record ID", example = "501")
        Long attendanceId,
        @Schema(description = "Attendance Date", example = "2026-09-09")
        LocalDate date,
        @Schema(description = "Attendance Status", example = "Present")
        String status,
        @Schema(description = "Punch-In Time", example = "09:00:00")
        LocalTime punchInTime,
        @Schema(description = "Punch-Out Time", example = "18:00:00")
        LocalTime punchOutTime,
        @Schema(description = "Notes", example = "Shift completed normally")
        String notes
    ) {}

    @Schema(description = "Team member schedule entry")
    public record TeamScheduleItemDto(
        @Schema(description = "Employee ID", example = "101")
        Long employeeId,
        @Schema(description = "Employee Full Name", example = "Jane Doe")
        String employeeName,
        @Schema(description = "Employee Email", example = "jane.doe@example.com")
        String employeeEmail,
        @Schema(description = "Today Schedule Details")
        Object todaySchedule
    ) {}

    @Schema(description = "Team member performance summary")
    public record TeamPerformanceItemDto(
        @Schema(description = "Employee ID", example = "101")
        Long employeeId,
        @Schema(description = "Employee Full Name", example = "Jane Doe")
        String employeeName,
        @Schema(description = "Employee Email", example = "jane.doe@example.com")
        String employeeEmail,
        @Schema(description = "Active Goals")
        List<?> goals,
        @Schema(description = "Feedback Loops")
        List<?> feedbacks
    ) {}

    @Schema(description = "Team member training summary")
    public record TeamTrainingItemDto(
        @Schema(description = "Employee ID", example = "101")
        Long employeeId,
        @Schema(description = "Employee Full Name", example = "Jane Doe")
        String employeeName,
        @Schema(description = "Employee Email", example = "jane.doe@example.com")
        String employeeEmail,
        @Schema(description = "Course Enrollments")
        List<?> enrollments
    ) {}

    @Schema(description = "Team member asset allocation")
    public record TeamAssetItemDto(
        @Schema(description = "Employee ID", example = "101")
        Long employeeId,
        @Schema(description = "Employee Full Name", example = "Jane Doe")
        String employeeName,
        @Schema(description = "Employee Email", example = "jane.doe@example.com")
        String employeeEmail,
        @Schema(description = "Allocated Assets")
        List<?> assets
    ) {}
}
