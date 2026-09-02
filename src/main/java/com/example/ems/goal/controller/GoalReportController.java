package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.dto.GoalResponse;
import com.example.ems.goal.service.GoalReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('GOAL_REPORT_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getAchievementReport(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status) {
        List<GoalResponse> report = reportService.getAchievementReport(category, priority, status);
        return ResponseEntity.ok(ApiResponse.success("Achievement report generated successfully", report));
    }

    @Operation(summary = "Overdue Goals Report", description = "Lists all overdue goals past target end date")
    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('GOAL_REPORT_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getOverdueReport() {
        List<GoalResponse> report = reportService.getOverdueReport();
        return ResponseEntity.ok(ApiResponse.success("Overdue goals report generated successfully", report));
    }
}
