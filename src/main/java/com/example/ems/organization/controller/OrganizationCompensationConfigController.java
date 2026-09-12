package com.example.ems.organization.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.organization.dto.CompensationConfigRequest;
import com.example.ems.organization.dto.CompensationConfigResponse;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.security.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/compensation-config")
@CrossOrigin("*")
@Tag(name = "Organization Compensation Configuration", description = "Endpoints for enabling or disabling variable compensation modules (Overtime, Incentive, Bonus) at tenant level.")
public class OrganizationCompensationConfigController {

    private final OrganizationCompensationConfigService configService;

    public OrganizationCompensationConfigController(OrganizationCompensationConfigService configService) {
        this.configService = configService;
    }

    @Operation(summary = "Get Compensation Configuration", description = "Retrieves variable compensation feature flags (OT, Incentive, Bonus) for the current organization.")
    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION_CONFIG_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompensationConfigResponse>> getCompensationConfig() {
        Long orgId = TenantContext.requireOrganizationId();
        CompensationConfigResponse response = configService.getConfig(orgId);
        return ResponseEntity.ok(ApiResponse.success("Compensation configuration retrieved successfully", response));
    }

    @Operation(summary = "Update Compensation Configuration", description = "Updates variable compensation feature flags (OT, Incentive, Bonus) for the current organization.")
    @PutMapping
    @PreAuthorize("hasAuthority('COMPENSATION_CONFIG_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompensationConfigResponse>> updateCompensationConfig(
            @RequestBody CompensationConfigRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        CompensationConfigResponse response = configService.updateConfig(orgId, request);
        return ResponseEntity.ok(ApiResponse.success("Compensation configuration updated successfully", response));
    }
}
