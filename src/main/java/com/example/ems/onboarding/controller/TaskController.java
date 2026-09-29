package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.task.*;
import com.example.ems.onboarding.service.OnboardingTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding/{onboardingId}/tasks")
@CrossOrigin("*")
@Tag(name = "Canonical Tasks Service")
public class TaskController {

    @Autowired
    private OnboardingTaskService taskService;

    @GetMapping
    @Operation(summary = "Get Onboarding Tasks List")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse>> getTasks(
            @PathVariable Long onboardingId) {
        OnboardingTaskListResponse response = taskService.getTasks(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", response));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get Onboarding Task Details")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse.TaskItem>> getTaskDetails(
            @PathVariable Long onboardingId,
            @PathVariable Long taskId) {
        OnboardingTaskListResponse.TaskItem response = taskService.getTaskDetails(onboardingId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Task details retrieved successfully", response));
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "Update Onboarding Task")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse.TaskItem>> updateTask(
            @PathVariable Long onboardingId,
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request) {
        OnboardingTaskListResponse.TaskItem response = taskService.updateTask(onboardingId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "Complete Onboarding Task")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse.TaskItem>> completeTask(
            @PathVariable Long onboardingId,
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskCompleteRequest request) {
        OnboardingTaskListResponse.TaskItem response = taskService.completeTask(onboardingId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully", response));
    }

    @PatchMapping("/{taskId}/assign")
    @Operation(summary = "Assign Onboarding Task")
    public ResponseEntity<ApiResponse<OnboardingTaskListResponse.TaskItem>> assignTask(
            @PathVariable Long onboardingId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignRequest request) {
        OnboardingTaskListResponse.TaskItem response = taskService.assignTask(onboardingId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully", response));
    }
}
