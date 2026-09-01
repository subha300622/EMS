package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.OnboardingAssignTemplateRequest;
import com.example.ems.onboarding.dto.OnboardingLaunchRequest;
import com.example.ems.onboarding.dto.OnboardingLaunchResponse;
import com.example.ems.onboarding.dto.OnboardingQueueResponse;
import com.example.ems.onboarding.dto.OnboardingStatsResponse;
import com.example.ems.onboarding.dto.OnboardingUpdateRequest;
import com.example.ems.onboarding.service.OnboardingWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Onboarding Workflow")
public class OnboardingWorkflowController {

    @Autowired
    private OnboardingWorkflowService workflowService;

    @GetMapping
    @Operation(summary = "Get Onboarding Queue")
    public ResponseEntity<ApiResponse<OnboardingQueueResponse>> getOnboardingQueue(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String joiningFrom,
            @RequestParam(required = false) String joiningTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        LocalDate fromDate = (joiningFrom != null && !joiningFrom.isBlank()) ? LocalDate.parse(joiningFrom) : null;
        LocalDate toDate = (joiningTo != null && !joiningTo.isBlank()) ? LocalDate.parse(joiningTo) : null;

        OnboardingQueueResponse response = workflowService.getOnboardingQueue(status, search, department, fromDate,
                toDate, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Onboarding records retrieved successfully", response));
    }

    @PostMapping
    @Operation(summary = "Launch Onboarding")
    public ResponseEntity<ApiResponse<OnboardingLaunchResponse>> launchOnboarding(
            @Valid @RequestBody OnboardingLaunchRequest request) {
        OnboardingLaunchResponse response = workflowService.launchOnboarding(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Onboarding launched successfully", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Onboarding Stats")
    public ResponseEntity<ApiResponse<OnboardingStatsResponse>> getStats() {
        OnboardingStatsResponse stats = workflowService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", stats));
    }

    @GetMapping("/{onboardingId}")
    @Operation(summary = "Get Onboarding Details")
    public ResponseEntity<ApiResponse<OnboardingQueueResponse.QueueItem>> getOnboardingDetails(
            @PathVariable Long onboardingId) {
        OnboardingQueueResponse.QueueItem response = workflowService.getOnboardingDetails(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Onboarding details retrieved successfully", response));
    }

    @PatchMapping("/{onboardingId}")
    @Operation(summary = "Update Onboarding Profile")
    public ResponseEntity<ApiResponse<OnboardingQueueResponse.QueueItem>> updateOnboarding(
            @PathVariable Long onboardingId,
            @RequestBody OnboardingUpdateRequest request) {
        OnboardingQueueResponse.QueueItem response = workflowService.updateOnboarding(onboardingId, request);
        return ResponseEntity.ok(ApiResponse.success("Onboarding profile updated successfully", response));
    }

    @DeleteMapping("/{onboardingId}")
    @Operation(summary = "Delete Onboarding Process")
    public ResponseEntity<ApiResponse<Void>> deleteOnboarding(
            @PathVariable Long onboardingId) {
        workflowService.deleteOnboarding(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Onboarding deleted successfully", null));
    }

    @PatchMapping("/{onboardingId}/template")
    @Operation(summary = "Assign or Replace Template")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> assignTemplate(
            @PathVariable Long onboardingId,
            @RequestBody OnboardingAssignTemplateRequest request) {
        java.util.Map<String, Object> response = workflowService.assignTemplate(onboardingId, request);
        return ResponseEntity.ok(ApiResponse.success("Template assigned/replaced successfully", response));
    }
}
