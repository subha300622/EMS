package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Action Link metadata")
public record EmployeeActionLinkDto(
        @Schema(description = "Action button or link label", example = "View Task")
        String label,

        @Schema(description = "Front-end navigation route for the action", example = "/employee/leave/501")
        String route
) {
}
