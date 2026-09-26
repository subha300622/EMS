package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pending Action or Task item")
public record EmployeeActionDto(
        @Schema(description = "Action task ID", example = "LEAVE-501")
        String id,

        @Schema(description = "Action category type", example = "LEAVE_APPROVAL")
        String type,

        @Schema(description = "Human-readable action title", example = "Leave request pending approval")
        String title,

        @Schema(description = "Action lifecycle status (PENDING, DUE_SOON, OVERDUE)", example = "PENDING")
        String status,

        @Schema(description = "Action priority (HIGH, NORMAL, LOW)", example = "NORMAL")
        String priority,

        @Schema(description = "Action due date in ISO-8601 format if applicable", example = "2026-09-25")
        String dueDate,

        @Schema(description = "Action context description", example = "Manager review required for your PTO")
        String description,

        @Schema(description = "Navigation action link")
        EmployeeActionLinkDto action
) {
}
