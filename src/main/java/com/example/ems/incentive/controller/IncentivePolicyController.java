package com.example.ems.incentive.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.dto.IncentivePolicyResponse;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import com.example.ems.incentive.service.IncentivePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/incentives/policies", "/api/v1/incentive-policies"})
@CrossOrigin("*")
@Tag(name = "Incentive Policy Management", description = "Endpoints for configuring organizational Incentive Policies, target slabs, calculation methods, and applicability filters.")
public class IncentivePolicyController {

    private final IncentivePolicyService policyService;

    public IncentivePolicyController(IncentivePolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(summary = "Create Incentive Policy", description = "Creates a new incentive policy in DRAFT status for the authenticated organization.")
    @PostMapping
    @PreAuthorize("hasAuthority('INCENTIVE_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> createPolicy(@Valid @RequestBody IncentivePolicyRequest request) {
        IncentivePolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incentive policy created successfully", response));
    }

    @Operation(summary = "Update Incentive Policy", description = "Updates an existing incentive policy.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INCENTIVE_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody IncentivePolicyRequest request) {
        IncentivePolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incentive policy updated successfully", response));
    }

    @Operation(summary = "Activate Incentive Policy", description = "Transitions an incentive policy to ACTIVE status.")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('INCENTIVE_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> activatePolicy(@PathVariable("id") Long id) {
        IncentivePolicyResponse response = policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Incentive policy activated successfully", response));
    }

    @Operation(summary = "Deactivate Incentive Policy", description = "Transitions an incentive policy to INACTIVE status.")
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('INCENTIVE_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> deactivatePolicy(@PathVariable("id") Long id) {
        IncentivePolicyResponse response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Incentive policy deactivated successfully", response));
    }

    @Operation(summary = "Archive Incentive Policy", description = "Transitions an incentive policy to ARCHIVED status.")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('INCENTIVE_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> archivePolicy(@PathVariable("id") Long id) {
        IncentivePolicyResponse response = policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Incentive policy archived successfully", response));
    }

    @Operation(summary = "Get Incentive Policy By ID", description = "Retrieves policy configuration details by ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<IncentivePolicyResponse>> getPolicyById(@PathVariable("id") Long id) {
        IncentivePolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success("Incentive policy retrieved successfully", response));
    }

    @Operation(summary = "Get All Incentive Policies", description = "Retrieves paginated incentive policies for the active organization.")
    @GetMapping
    @PreAuthorize("hasAuthority('INCENTIVE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<IncentivePolicyResponse>>> getAllPolicies(
            @RequestParam(value = "status", required = false) IncentivePolicyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "id"));
        Page<IncentivePolicyResponse> response = policyService.getAllPolicies(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Incentive policies retrieved successfully", response));
    }
}
