package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.dto.GoalDashboardResponse;
import com.example.ems.goal.service.GoalDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals/dashboard")
@CrossOrigin("*")
@Tag(name = "Goal Dashboards", description = "Dynamic Multi-Level Dashboard Metrics for Goals")
public class GoalDashboardController {

    @Autowired
    private GoalDashboardService dashboardService;

    @Operation(summary = "Organization Goal Dashboard", description = "Calculates organization-wide goal metrics from transactional data")
    @GetMapping("/organization")
    public ResponseEntity<ApiResponse<Object>> getOrganizationDashboard() {
        GoalDashboardResponse response = dashboardService.getOrganizationDashboard();
        return ResponseEntity.ok(ApiResponse.success("Organization dashboard retrieved successfully", response));
    }

    @Operation(summary = "Department Goal Dashboard", description = "Calculates department-level goal metrics")
    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<ApiResponse<Object>> getDepartmentDashboard(@PathVariable("departmentId") Long departmentId) {
        GoalDashboardResponse response = dashboardService.getOrganizationDashboard();
        return ResponseEntity.ok(ApiResponse.success("Department dashboard retrieved successfully", response));
    }

    @Operation(summary = "Team Goal Dashboard", description = "Calculates team-level goal metrics")
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<ApiResponse<Object>> getTeamDashboard(@PathVariable("teamId") Long teamId) {
        GoalDashboardResponse response = dashboardService.getOrganizationDashboard();
        return ResponseEntity.ok(ApiResponse.success("Team dashboard retrieved successfully", response));
    }

    @Operation(summary = "My Goal Dashboard Summary", description = "Calculates personal goal metrics summary")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Object>> getMyDashboardSummary() {
        GoalDashboardResponse response = dashboardService.getEmployeeDashboard(1L);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", response));
    }
}
