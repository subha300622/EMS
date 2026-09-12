package com.example.ems.bonus.controller;

import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.dto.BonusPolicyResponse;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.bonus.service.BonusPolicyService;
import com.example.ems.common.dto.ApiResponse;
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
@RequestMapping({"/api/v1/bonuses/policies", "/api/v1/bonus-policies"})
@CrossOrigin("*")
@Tag(name = "Bonus Policy Management", description = "Endpoints for configuring organizational Bonus Policies, calculation methods, and eligibility rules.")
public class BonusPolicyController {

    private final BonusPolicyService policyService;

    public BonusPolicyController(BonusPolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(summary = "Create Bonus Policy", description = "Creates a new bonus policy in DRAFT status for the authenticated organization.")
    @PostMapping
    @PreAuthorize("hasAuthority('BONUS_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> createPolicy(@Valid @RequestBody BonusPolicyRequest request) {
        BonusPolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bonus policy created successfully", response));
    }

    @Operation(summary = "Update Bonus Policy", description = "Updates an existing bonus policy (only allowed in DRAFT status).")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BONUS_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody BonusPolicyRequest request) {
        BonusPolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bonus policy updated successfully", response));
    }

    @Operation(summary = "Activate Bonus Policy", description = "Transitions a bonus policy to ACTIVE status.")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('BONUS_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> activatePolicy(@PathVariable("id") Long id) {
        BonusPolicyResponse response = policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus policy activated successfully", response));
    }

    @Operation(summary = "Deactivate Bonus Policy", description = "Transitions a bonus policy to INACTIVE status.")
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('BONUS_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> deactivatePolicy(@PathVariable("id") Long id) {
        BonusPolicyResponse response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus policy deactivated successfully", response));
    }

    @Operation(summary = "Archive Bonus Policy", description = "Transitions a bonus policy to ARCHIVED status.")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('BONUS_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> archivePolicy(@PathVariable("id") Long id) {
        BonusPolicyResponse response = policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus policy archived successfully", response));
    }

    @Operation(summary = "Get Bonus Policy By ID", description = "Retrieves bonus policy configuration details by ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BonusPolicyResponse>> getPolicyById(@PathVariable("id") Long id) {
        BonusPolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success("Bonus policy retrieved successfully", response));
    }

    @Operation(summary = "Get All Bonus Policies", description = "Retrieves paginated bonus policies for the active organization.")
    @GetMapping
    @PreAuthorize("hasAuthority('BONUS_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<BonusPolicyResponse>>> getAllPolicies(
            @RequestParam(value = "status", required = false) BonusPolicyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "id"));
        Page<BonusPolicyResponse> response = policyService.getAllPolicies(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Bonus policies retrieved successfully", response));
    }
}
