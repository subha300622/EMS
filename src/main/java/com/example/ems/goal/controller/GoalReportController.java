package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.service.GoalReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/reports")
@CrossOrigin("*")
@Tag(name = "Goal Reports", description = "Enterprise Reporting & Filtered Exports for Goal Management")
public class GoalReportController {

    @Autowired
    private GoalReportService reportService;

    @Operation(summary = "Goal Achievement Report", description = "Generates achievement dataset filtered by category, priority, or status")
    @GetMapping("/achievement")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAchievementReport(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status) {
        List<GoalResponse> report = reportService.getAchievementReport(category, priority, status);
        return ResponseEntity.ok(ApiResponse.success("Achievement report generated successfully", report));
    }

    @Operation(summary = "Overdue Goals Report", description = "Lists all overdue goals past target end date")
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getOverdueReport() {
        List<GoalResponse> report = reportService.getOverdueReport();
        return ResponseEntity.ok(ApiResponse.success("Overdue goals report generated successfully", report));
    }

    @Operation(summary = "Progress Report", description = "Generates progress trends report across goals")
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getProgressReport() {
        List<GoalResponse> report = reportService.getProgressReport();
        return ResponseEntity.ok(ApiResponse.success("Progress report generated successfully", report));
    }

    @Operation(summary = "Employee Performance Report", description = "Generates goal achievement report per employee")
    @GetMapping("/employee-performance")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getEmployeePerformanceReport(
            @RequestParam(required = false) Long employeeId) {
        List<GoalResponse> report = reportService.getEmployeePerformanceReport(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee performance report generated successfully", report));
    }

    @Operation(summary = "Team Performance Report", description = "Generates goal achievement report per team")
    @GetMapping("/team-performance")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getTeamPerformanceReport(
            @RequestParam(required = false) Long teamId) {
        List<GoalResponse> report = reportService.getTeamPerformanceReport(teamId);
        return ResponseEntity.ok(ApiResponse.success("Team performance report generated successfully", report));
    }

    @Operation(summary = "Department Performance Report", description = "Generates goal achievement report per department")
    @GetMapping("/department-performance")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getDepartmentPerformanceReport(
            @RequestParam(required = false) Long departmentId) {
        List<GoalResponse> report = reportService.getDepartmentPerformanceReport(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department performance report generated successfully", report));
    }
}
