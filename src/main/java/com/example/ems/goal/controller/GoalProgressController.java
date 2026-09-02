package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalProgress;
import com.example.ems.goal.dto.GoalProgressRequest;
import com.example.ems.goal.service.GoalProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/progress")
@CrossOrigin("*")
@Tag(name = "Goal Progress Management", description = "APIs for Progress Updates and Parent Rollup")
public class GoalProgressController {

    @Autowired
    private GoalProgressService progressService;

    @Operation(summary = "Add Progress Update", description = "Appends an immutable progress history entry and recalculates weighted parent progress")
    @PostMapping
    @PreAuthorize("hasAuthority('GOAL_PROGRESS_UPDATE') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> addProgressUpdate(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalProgressRequest request) {
        GoalProgress entry = progressService.addProgressUpdate(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", entry));
    }

    @Operation(summary = "Get Progress History", description = "Retrieves progress history trend for a goal")
    @GetMapping
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getProgressHistory(@PathVariable("goalId") Long goalId) {
        List<GoalProgress> history = progressService.getProgressHistory(goalId);
        return ResponseEntity.ok(ApiResponse.success("Progress history retrieved successfully", history));
    }
}
