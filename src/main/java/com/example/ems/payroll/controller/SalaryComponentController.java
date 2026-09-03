package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.SalaryComponentCreateRequest;
import com.example.ems.payroll.dto.SalaryComponentResponse;
import com.example.ems.payroll.dto.SalaryComponentUpdateRequest;
import com.example.ems.payroll.entity.SalaryComponentType;
import com.example.ems.payroll.service.SalaryComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salary-components")
@CrossOrigin("*")
@Tag(name = "Salary Components", description = "Organization-Level Customizable Salary Components Catalog")
public class SalaryComponentController {

    @Autowired
    private SalaryComponentService salaryComponentService;

    @Operation(summary = "Create Salary Component", description = "Creates a new reusable salary component in the organization catalog.")
    @PostMapping
    @PreAuthorize("hasAuthority('SALARY_COMPONENT_CREATE')")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> createComponent(
            @Valid @RequestBody SalaryComponentCreateRequest request) {
        SalaryComponentResponse response = salaryComponentService.createComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary component created successfully", response));
    }

    @Operation(summary = "Get All Salary Components", description = "Retrieves salary components for the active tenant with optional type and active filters.")
    @GetMapping
    @PreAuthorize("hasAuthority('SALARY_COMPONENT_VIEW')")
    public ResponseEntity<ApiResponse<List<SalaryComponentResponse>>> getComponents(
            @RequestParam(required = false) SalaryComponentType type,
            @RequestParam(required = false) Boolean active) {
        List<SalaryComponentResponse> list = salaryComponentService.getComponents(type, active);
        return ResponseEntity.ok(ApiResponse.success("Salary components retrieved successfully", list));
    }

    @Operation(summary = "Get Salary Component by ID", description = "Retrieves a single salary component by its ID for the active tenant.")
    @GetMapping("/{componentId}")
    @PreAuthorize("hasAuthority('SALARY_COMPONENT_VIEW')")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> getComponentById(
            @PathVariable Long componentId) {
        SalaryComponentResponse response = salaryComponentService.getComponentById(componentId);
        return ResponseEntity.ok(ApiResponse.success("Salary component retrieved successfully", response));
    }

    @Operation(summary = "Update Salary Component", description = "Updates an existing salary component's details in the organization catalog.")
    @PutMapping("/{componentId}")
    @PreAuthorize("hasAuthority('SALARY_COMPONENT_UPDATE')")
    public ResponseEntity<ApiResponse<SalaryComponentResponse>> updateComponent(
            @PathVariable Long componentId,
            @Valid @RequestBody SalaryComponentUpdateRequest request) {
        SalaryComponentResponse response = salaryComponentService.updateComponent(componentId, request);
        return ResponseEntity.ok(ApiResponse.success("Salary component updated successfully", response));
    }

    @Operation(summary = "Deactivate Salary Component", description = "Soft-deletes/deactivates a salary component so historical records remain intact.")
    @DeleteMapping("/{componentId}")
    @PreAuthorize("hasAuthority('SALARY_COMPONENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(
            @PathVariable Long componentId) {
        salaryComponentService.deactivateComponent(componentId);
        return ResponseEntity.ok(ApiResponse.success("Salary component deactivated successfully", null));
    }
}
