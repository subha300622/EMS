package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.*;
import com.example.ems.goal.service.GoalConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goal-config")
@CrossOrigin("*")
@Tag(name = "Goal Module Configuration", description = "Organization-Customizable Rules, Categories, Priorities, and Settings")
public class GoalConfigController {

    @Autowired
    private GoalConfigService configService;

    @Operation(summary = "Get Organization Goal Config", description = "Retrieves org-level goal module settings")
    @GetMapping
    @PreAuthorize("hasAuthority('GOAL_CONFIG_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getConfig() {
        GoalConfig config = configService.getOrCreateConfig();
        return ResponseEntity.ok(ApiResponse.success("Goal config retrieved successfully", config));
    }

    @Operation(summary = "Get Active Goal Categories", description = "Lists active goal categories for active tenant")
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getCategories() {
        List<GoalCategory> categories = configService.getCategories();
        return ResponseEntity.ok(ApiResponse.success("Goal categories retrieved successfully", categories));
    }

    @Operation(summary = "Get Active Goal Types", description = "Lists active goal types for active tenant")
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getTypes() {
        List<GoalTypeEntity> types = configService.getTypes();
        return ResponseEntity.ok(ApiResponse.success("Goal types retrieved successfully", types));
    }

    @Operation(summary = "Get Active Goal Priorities", description = "Lists active goal priorities sorted by order")
    @GetMapping("/priorities")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getPriorities() {
        List<GoalPriorityEntity> priorities = configService.getPriorities();
        return ResponseEntity.ok(ApiResponse.success("Goal priorities retrieved successfully", priorities));
    }

    @Operation(summary = "Get Active Goal Statuses", description = "Lists active goal system/custom statuses")
    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getStatuses() {
        List<GoalStatusEntity> statuses = configService.getStatuses();
        return ResponseEntity.ok(ApiResponse.success("Goal statuses retrieved successfully", statuses));
    }

    @Operation(summary = "Get Goal Visibilities", description = "Lists active goal visibility options")
    @GetMapping("/visibilities")
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getVisibilities() {
        List<GoalVisibilitySetting> visibilities = configService.getVisibilities();
        return ResponseEntity.ok(ApiResponse.success("Goal visibilities retrieved successfully", visibilities));
    }

    @Operation(summary = "Get Goal Assignment Rules", description = "Lists goal assignment rules for active tenant")
    @GetMapping("/assignment-rules")
    @PreAuthorize("hasAuthority('GOAL_CONFIG_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getAssignmentRules() {
        List<GoalAssignmentRule> rules = configService.getAssignmentRules();
        return ResponseEntity.ok(ApiResponse.success("Goal assignment rules retrieved successfully", rules));
    }

    @Operation(summary = "Get Goal Notification Settings", description = "Lists goal notification settings for active tenant")
    @GetMapping("/notifications")
    @PreAuthorize("hasAuthority('GOAL_CONFIG_VIEW') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> getNotificationSettings() {
        List<GoalNotificationSetting> settings = configService.getNotificationSettings();
        return ResponseEntity.ok(ApiResponse.success("Goal notification settings retrieved successfully", settings));
    }
}
