package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalMilestone;
import com.example.ems.goal.dto.GoalMilestoneRequest;
import com.example.ems.goal.service.GoalMilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/milestones")
@CrossOrigin("*")
@Tag(name = "Goal Milestone Management", description = "APIs for Goal Sub-tasks / Milestones")
public class GoalMilestoneController {

    @Autowired
    private GoalMilestoneService milestoneService;

    @Operation(summary = "Add Milestone", description = "Adds a new milestone to a goal")
    @PostMapping
    @PreAuthorize("hasAuthority('GOAL_EDIT') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> addMilestone(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalMilestoneRequest request) {
        GoalMilestone milestone = milestoneService.addMilestone(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone created successfully", milestone));
    }

    @Operation(summary = "Get Milestones", description = "Lists milestones for a goal")
    @GetMapping
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getMilestones(@PathVariable("goalId") Long goalId) {
        List<GoalMilestone> milestones = milestoneService.getMilestones(goalId);
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestones));
    }

    @Operation(summary = "Complete Milestone", description = "Marks a milestone as COMPLETED")
    @PostMapping("/{milestoneId}/complete")
    @PreAuthorize("hasAuthority('GOAL_EDIT') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> completeMilestone(
            @PathVariable("goalId") Long goalId,
            @PathVariable("milestoneId") Long milestoneId) {
        GoalMilestone milestone = milestoneService.completeMilestone(milestoneId, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone completed successfully", milestone));
    }
}
