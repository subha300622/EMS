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
    public ResponseEntity<ApiResponse<Object>> addMilestone(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalMilestoneRequest request) {
        GoalMilestone milestone = milestoneService.addMilestone(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone created successfully", milestone));
    }

    @Operation(summary = "Get Milestones", description = "Lists milestones for a goal")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMilestones(@PathVariable("goalId") Long goalId) {
        List<GoalMilestone> milestones = milestoneService.getMilestones(goalId);
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully", milestones));
    }

    @Operation(summary = "Get Milestone by ID", description = "Retrieves details of a specific milestone")
    @GetMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Object>> getMilestoneById(
            @PathVariable("goalId") Long goalId,
            @PathVariable("milestoneId") Long milestoneId) {
        GoalMilestone milestone = milestoneService.getMilestoneById(milestoneId);
        return ResponseEntity.ok(ApiResponse.success("Milestone retrieved successfully", milestone));
    }

    @Operation(summary = "Update Milestone", description = "Updates details of a milestone")
    @PutMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Object>> updateMilestone(
            @PathVariable("goalId") Long goalId,
            @PathVariable("milestoneId") Long milestoneId,
            @RequestBody GoalMilestoneRequest request) {
        GoalMilestone milestone = milestoneService.updateMilestone(milestoneId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone updated successfully", milestone));
    }

    @Operation(summary = "Delete Milestone", description = "Deletes a milestone")
    @DeleteMapping("/{milestoneId}")
    public ResponseEntity<ApiResponse<Object>> deleteMilestone(
            @PathVariable("goalId") Long goalId,
            @PathVariable("milestoneId") Long milestoneId) {
        milestoneService.deleteMilestone(milestoneId, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted successfully", null));
    }

    @Operation(summary = "Complete Milestone", description = "Marks a milestone as COMPLETED")
    @PostMapping("/{milestoneId}/complete")
    public ResponseEntity<ApiResponse<Object>> completeMilestone(
            @PathVariable("goalId") Long goalId,
            @PathVariable("milestoneId") Long milestoneId) {
        GoalMilestone milestone = milestoneService.completeMilestone(milestoneId, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Milestone completed successfully", milestone));
    }
}
