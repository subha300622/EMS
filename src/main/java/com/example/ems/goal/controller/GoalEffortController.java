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
    public ResponseEntity<ApiResponse<Object>> logEffort(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalEffortRequest request) {
        GoalEffort entry = effortService.logEffort(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Effort logged successfully", entry));
    }

    @Operation(summary = "Get Effort Entries", description = "Lists effort log entries for a goal")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getEffortEntries(@PathVariable("goalId") Long goalId) {
        List<GoalEffort> entries = effortService.getEffortEntries(goalId);
        return ResponseEntity.ok(ApiResponse.success("Effort entries retrieved successfully", entries));
    }

    @Operation(summary = "Get Effort Entry by ID", description = "Retrieves a specific effort log entry")
    @GetMapping("/{effortId}")
    public ResponseEntity<ApiResponse<Object>> getEffortById(
            @PathVariable("goalId") Long goalId,
            @PathVariable("effortId") Long effortId) {
        GoalEffort entry = effortService.getEffortById(effortId);
        return ResponseEntity.ok(ApiResponse.success("Effort entry retrieved successfully", entry));
    }

    @Operation(summary = "Update Effort Entry", description = "Updates an existing effort log entry")
    @PutMapping("/{effortId}")
    public ResponseEntity<ApiResponse<Object>> updateEffort(
            @PathVariable("goalId") Long goalId,
            @PathVariable("effortId") Long effortId,
            @RequestBody GoalEffortRequest request) {
        GoalEffort entry = effortService.updateEffort(effortId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Effort entry updated successfully", entry));
    }

    @Operation(summary = "Delete Effort Entry", description = "Deletes an effort log entry")
    @DeleteMapping("/{effortId}")
    public ResponseEntity<ApiResponse<Object>> deleteEffort(
            @PathVariable("goalId") Long goalId,
            @PathVariable("effortId") Long effortId) {
        effortService.deleteEffort(effortId, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Effort entry deleted successfully", null));
    }
}
