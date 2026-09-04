package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalAssignment;
import com.example.ems.goal.domain.GoalAssignmentHistory;
import com.example.ems.goal.dto.GoalAssignRequest;
import com.example.ems.goal.service.GoalAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/{goalId}")
@CrossOrigin("*")
@Tag(name = "Goal Assignment Management", description = "APIs for Assigning and Reassigning Goals")
public class GoalAssignmentController {

    @Autowired
    private GoalAssignmentService assignmentService;

    @Operation(summary = "Assign Goal", description = "Assigns goal to organization, branch, department, team, employee, or project level")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('GOAL_ASSIGN') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> assignGoal(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalAssignRequest request) {
        GoalAssignment assignment = assignmentService.assignGoal(goalId, request, 1L, "System", "ADMIN");
        return ResponseEntity.ok(ApiResponse.success("Goal assigned successfully", assignment));
    }

    @Operation(summary = "Reassign Goal", description = "Reassigns goal and logs assignment history")
    @PostMapping("/reassign")
    @PreAuthorize("hasAuthority('GOAL_ASSIGN') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> reassignGoal(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalAssignRequest request) {
        GoalAssignment assignment = assignmentService.reassignGoal(goalId, request, 1L, "System", "ADMIN");
        return ResponseEntity.ok(ApiResponse.success("Goal reassigned successfully", assignment));
    }

    @Operation(summary = "Get Goal Active Assignments", description = "Lists current active assignments for a goal")
    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getAssignments(@PathVariable("goalId") Long goalId) {
        List<GoalAssignment> assignments = assignmentService.getActiveAssignments(goalId);
        return ResponseEntity.ok(ApiResponse.success("Assignments retrieved successfully", assignments));
    }

    @Operation(summary = "Get Goal Assignment History", description = "Lists historical assignment audit trail for a goal")
    @GetMapping("/assignment-history")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getAssignmentHistory(@PathVariable("goalId") Long goalId) {
        List<GoalAssignmentHistory> history = assignmentService.getAssignmentHistory(goalId);
        return ResponseEntity.ok(ApiResponse.success("Assignment history retrieved successfully", history));
    }
}
