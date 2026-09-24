package com.example.ems.employee.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Action Center aggregation response")
public record EmployeeActionCenterResponseDto(
        @Schema(description = "Total number of actionable items", example = "4")
        Integer total,

        @Schema(description = "List of pending actionable tasks")
        List<EmployeeActionDto> items
) {
}
