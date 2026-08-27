package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.policy.*;
import com.example.ems.onboarding.entity.OnboardingApprovalPolicy;
import com.example.ems.onboarding.service.OnboardingApprovalPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/onboarding")
@CrossOrigin("*")
@Tag(name = "Onboarding Approval Policy Engine")
public class OnboardingApprovalPolicyController {

    @Autowired
    private OnboardingApprovalPolicyService policyService;

    @GetMapping("/policies/status-transitions")
    @Operation(summary = "Get Status Transition Policy Template Matrix")
    public ResponseEntity<ApiResponse<List<OnboardingStatusTransitionPolicyDto>>> getStatusTransitionPolicies() {
        List<OnboardingStatusTransitionPolicyDto> policies = policyService.getStatusTransitionPolicies();
        return ResponseEntity.ok(ApiResponse.success("Status transition policies retrieved successfully", policies));
    }

    @GetMapping("/approval-policies")
    @Operation(summary = "List Active Tenant Approval Policies")
    public ResponseEntity<ApiResponse<List<OnboardingApprovalPolicy>>> getApprovalPolicies() {
        List<OnboardingApprovalPolicy> policies = policyService.getApprovalPoliciesForCurrentTenant();
        return ResponseEntity.ok(ApiResponse.success("Approval policies retrieved successfully", policies));
    }

    @PostMapping("/approval-policies")
    @Operation(summary = "Create Organization Approval Policy")
    public ResponseEntity<ApiResponse<OnboardingApprovalPolicy>> createApprovalPolicy(
            @Valid @RequestBody OnboardingApprovalPolicyRequest request) {
        OnboardingApprovalPolicy policy = policyService.createApprovalPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Approval policy created successfully", policy));
    }

    @PatchMapping("/approval-policies/{policyId}")
    @Operation(summary = "Update Organization Approval Policy")
    public ResponseEntity<ApiResponse<OnboardingApprovalPolicy>> updateApprovalPolicy(
            @PathVariable Long policyId,
            @RequestBody OnboardingApprovalPolicyRequest request) {
        OnboardingApprovalPolicy policy = policyService.updateApprovalPolicy(policyId, request);
        return ResponseEntity.ok(ApiResponse.success("Approval policy updated successfully", policy));
    }

    @GetMapping("/{onboardingId}/approval-eligibility")
    @Operation(summary = "Check Approval Eligibility for Candidate Onboarding")
    public ResponseEntity<ApiResponse<OnboardingApprovalEligibilityResponse>> checkEligibility(
            @PathVariable Long onboardingId) {
        OnboardingApprovalEligibilityResponse response = policyService.checkEligibility(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Approval eligibility evaluated successfully", response));
    }

    @PostMapping("/{onboardingId}/approve")
    @Operation(summary = "Approve Candidate Onboarding via Approval Policy Engine")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveOnboarding(
            @PathVariable Long onboardingId,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.get("remarks") : "Approved via Approval Policy Engine";
        Map<String, Object> result = policyService.approveOnboardingWithPolicy(onboardingId, remarks);
        return ResponseEntity.ok(ApiResponse.success("Onboarding approved successfully", result));
    }
}
