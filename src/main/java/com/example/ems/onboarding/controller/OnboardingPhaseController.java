package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.phase.OnboardingPhaseListResponse;
import com.example.ems.onboarding.dto.phase.PhaseUpdateRequest;
import com.example.ems.onboarding.service.OnboardingPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding/{onboardingId}/phases")
@CrossOrigin("*")
@Tag(name = "Onboarding Phase Management")
public class OnboardingPhaseController {

    @Autowired
    private OnboardingPhaseService phaseService;

    @GetMapping
    @Operation(summary = "Get Onboarding Phase List")
    public ResponseEntity<ApiResponse<OnboardingPhaseListResponse>> getPhases(
            @PathVariable Long onboardingId) {
        OnboardingPhaseListResponse response = phaseService.getPhases(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Phases retrieved successfully", response));
    }

    @GetMapping("/{phaseId}")
    @Operation(summary = "Get Onboarding Phase Details")
    public ResponseEntity<ApiResponse<OnboardingPhaseListResponse.PhaseItem>> getPhaseDetails(
            @PathVariable Long onboardingId,
            @PathVariable Long phaseId) {
        OnboardingPhaseListResponse.PhaseItem response = phaseService.getPhaseDetails(onboardingId, phaseId);
        return ResponseEntity.ok(ApiResponse.success("Phase details retrieved successfully", response));
    }

    @PatchMapping("/{phaseId}")
    @Operation(summary = "Update Onboarding Phase")
    public ResponseEntity<ApiResponse<OnboardingPhaseListResponse.PhaseItem>> updatePhase(
            @PathVariable Long onboardingId,
            @PathVariable Long phaseId,
            @RequestBody PhaseUpdateRequest request) {
        OnboardingPhaseListResponse.PhaseItem response = phaseService.updatePhase(onboardingId, phaseId, request);
        return ResponseEntity.ok(ApiResponse.success("Phase updated successfully", response));
    }
}
