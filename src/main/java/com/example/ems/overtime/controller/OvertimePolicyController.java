package com.example.ems.overtime.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.dto.OvertimePolicyResponse;
import com.example.ems.overtime.entity.OvertimePolicyStatus;
import com.example.ems.overtime.service.OvertimePolicyService;
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
@RequestMapping("/api/v1/overtime/policies")
@CrossOrigin("*")
@Tag(name = "Overtime Policy Management", description = "Endpoints for configuring organizational Overtime Policies, multipliers, thresholds, basis, and applicability filters.")
public class OvertimePolicyController {

    private final OvertimePolicyService policyService;

    public OvertimePolicyController(OvertimePolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(summary = "Create Overtime Policy", description = "Creates a new overtime policy in DRAFT status for the authenticated organization.")
    @PostMapping
    @PreAuthorize("hasAuthority('OVERTIME_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> createPolicy(@Valid @RequestBody OvertimePolicyRequest request) {
        OvertimePolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Overtime policy created successfully", response));
    }

    @Operation(summary = "Update Overtime Policy", description = "Updates an existing overtime policy.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OVERTIME_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody OvertimePolicyRequest request) {
        OvertimePolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Overtime policy updated successfully", response));
    }

    @Operation(summary = "Activate Overtime Policy", description = "Transitions an overtime policy to ACTIVE status.")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('OVERTIME_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> activatePolicy(@PathVariable("id") Long id) {
        OvertimePolicyResponse response = policyService.activatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Overtime policy activated successfully", response));
    }

    @Operation(summary = "Deactivate Overtime Policy", description = "Transitions an overtime policy to INACTIVE status.")
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('OVERTIME_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> deactivatePolicy(@PathVariable("id") Long id) {
        OvertimePolicyResponse response = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Overtime policy deactivated successfully", response));
    }

    @Operation(summary = "Archive Overtime Policy", description = "Transitions an overtime policy to ARCHIVED status.")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('OVERTIME_POLICY_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> archivePolicy(@PathVariable("id") Long id) {
        OvertimePolicyResponse response = policyService.archivePolicy(id);
        return ResponseEntity.ok(ApiResponse.success("Overtime policy archived successfully", response));
    }

    @Operation(summary = "Get Overtime Policy By ID", description = "Retrieves policy configuration details by ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OvertimePolicyResponse>> getPolicyById(@PathVariable("id") Long id) {
        OvertimePolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success("Overtime policy retrieved successfully", response));
    }

    @Operation(summary = "Get All Overtime Policies", description = "Retrieves paginated overtime policies for the active organization.")
    @GetMapping
    @PreAuthorize("hasAuthority('OVERTIME_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OvertimePolicyResponse>>> getAllPolicies(
            @RequestParam(value = "status", required = false) OvertimePolicyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "id"));
        Page<OvertimePolicyResponse> response = policyService.getAllPolicies(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Overtime policies retrieved successfully", response));
    }
}
