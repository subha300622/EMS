package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalEffort;
import com.example.ems.goal.dto.GoalEffortRequest;
import com.example.ems.goal.service.GoalEffortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/efforts")
@CrossOrigin("*")
@Tag(name = "Goal Effort Tracking", description = "APIs for Actual Hours Logging and Efficiency Calculation")
public class GoalEffortController {

    @Autowired
    private GoalEffortService effortService;

    @Operation(summary = "Log Effort Hours", description = "Logs actual effort hours for a goal")
    @PostMapping
    @PreAuthorize("hasAuthority('GOAL_EFFORT_LOG') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> logEffort(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalEffortRequest request) {
        GoalEffort entry = effortService.logEffort(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Effort logged successfully", entry));
    }

    @Operation(summary = "Get Effort Entries", description = "Lists effort log entries for a goal")
    @GetMapping
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getEffortEntries(@PathVariable("goalId") Long goalId) {
        List<GoalEffort> entries = effortService.getEffortEntries(goalId);
        return ResponseEntity.ok(ApiResponse.success("Effort entries retrieved successfully", entries));
    }
}
