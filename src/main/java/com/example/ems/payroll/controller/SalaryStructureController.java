package com.example.ems.payroll.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.payroll.dto.SalaryStructureCreateRequest;
import com.example.ems.payroll.dto.SalaryStructureResponse;
import com.example.ems.payroll.dto.SalaryStructureUpdateRequest;
import com.example.ems.payroll.entity.SalaryStructureStatus;
import com.example.ems.payroll.dto.SalaryDependencyGraphResponse;
import com.example.ems.payroll.dto.StructureComponentCreateRequest;
import com.example.ems.payroll.dto.StructureComponentResponse;
import com.example.ems.payroll.dto.StructureComponentUpdateRequest;
import com.example.ems.payroll.service.SalaryStructureComponentService;
import com.example.ems.payroll.service.SalaryStructureService;
import com.example.ems.payroll.service.SalaryStructureValidationService;
import com.example.ems.payroll.validation.SalaryValidationResult;
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
@RequestMapping("/api/v1/salary-structures")
@CrossOrigin("*")
@Tag(name = "Salary Structures", description = "Organization-Level Customizable Salary Structure Templates and Lifecycle")
public class SalaryStructureController {

    @Autowired
    private SalaryStructureService salaryStructureService;

    @Autowired
    private SalaryStructureComponentService salaryStructureComponentService;

    @Autowired
    private SalaryStructureValidationService salaryStructureValidationService;

    @Operation(summary = "Create Salary Structure", description = "Creates a new salary structure template in DRAFT status with version 1.")
    @PostMapping
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_CREATE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> createStructure(
            @Valid @RequestBody SalaryStructureCreateRequest request) {
        SalaryStructureResponse response = salaryStructureService.createStructure(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary structure created successfully in DRAFT status", response));
    }

    @Operation(summary = "Get All Salary Structures", description = "Lists salary structures for the active tenant with optional status and search filters.")
    @GetMapping
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_VIEW')")
    public ResponseEntity<ApiResponse<List<SalaryStructureResponse>>> getStructures(
            @RequestParam(required = false) SalaryStructureStatus status,
            @RequestParam(required = false) String search) {
        List<SalaryStructureResponse> list = salaryStructureService.getStructures(status, search);
        return ResponseEntity.ok(ApiResponse.success("Salary structures retrieved successfully", list));
    }

    @Operation(summary = "Get Salary Structure by ID", description = "Retrieves a single salary structure by its ID for the active tenant.")
    @GetMapping("/{structureId}")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_VIEW')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> getStructureById(
            @PathVariable Long structureId) {
        SalaryStructureResponse response = salaryStructureService.getStructureById(structureId);
        return ResponseEntity.ok(ApiResponse.success("Salary structure retrieved successfully", response));
    }

    @Operation(summary = "Update Salary Structure", description = "Updates metadata of a DRAFT or VALIDATED salary structure.")
    @PutMapping("/{structureId}")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_UPDATE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> updateStructure(
            @PathVariable Long structureId,
            @Valid @RequestBody SalaryStructureUpdateRequest request) {
        SalaryStructureResponse response = salaryStructureService.updateStructure(structureId, request);
        return ResponseEntity.ok(ApiResponse.success("Salary structure updated successfully", response));
    }

    @Operation(summary = "Deactivate/Delete Salary Structure", description = "Soft-deletes a salary structure by setting its status to INACTIVE.")
    @DeleteMapping("/{structureId}")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteStructure(
            @PathVariable Long structureId) {
        salaryStructureService.deleteStructure(structureId);
        return ResponseEntity.ok(ApiResponse.success("Salary structure deactivated successfully", null));
    }

    @Operation(summary = "Validate Salary Structure", description = "Validates structure integrity, detects circular dependencies via DAG, auto-orders calculation sequence, and transitions status from DRAFT to VALIDATED.")
    @PostMapping("/{structureId}/validate")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_VALIDATE')")
    public ResponseEntity<ApiResponse<SalaryValidationResult>> validateStructure(
            @PathVariable Long structureId) {
        SalaryValidationResult result = salaryStructureValidationService.validateStructure(structureId);
        String message = result.isValid() ? "Salary structure validated successfully" : "Salary structure validation failed";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @Operation(summary = "Activate Salary Structure", description = "Re-validates all component calculation rules and activates a salary structure for employee assignment and calculations.")
    @PostMapping("/{structureId}/activate")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_ACTIVATE')")
    public ResponseEntity<ApiResponse<SalaryValidationResult>> activateStructure(
            @PathVariable Long structureId) {
        SalaryValidationResult result = salaryStructureValidationService.activateStructure(structureId);
        return ResponseEntity.ok(ApiResponse.success("Salary structure activated successfully", result));
    }

    @Operation(summary = "Get Salary Structure Dependency Graph", description = "Diagnostic endpoint to preview component dependencies, cycle detection, and calculation ordering without modifying state.")
    @GetMapping("/{structureId}/dependency-graph")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_VIEW')")
    public ResponseEntity<ApiResponse<SalaryDependencyGraphResponse>> getDependencyGraph(
            @PathVariable Long structureId) {
        SalaryDependencyGraphResponse response = salaryStructureValidationService.getDependencyGraph(structureId);
        return ResponseEntity.ok(ApiResponse.success("Dependency graph retrieved successfully", response));
    }

    @Operation(summary = "Deactivate Salary Structure", description = "Deactivates an active salary structure so no new employee assignments can use it.")
    @PostMapping("/{structureId}/deactivate")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_DEACTIVATE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> deactivateStructure(
            @PathVariable Long structureId) {
        SalaryStructureResponse response = salaryStructureService.deactivateStructure(structureId);
        return ResponseEntity.ok(ApiResponse.success("Salary structure deactivated successfully", response));
    }

    @Operation(summary = "Create New Version of Structure", description = "Branches an existing structure into a new DRAFT version (version + 1) to protect historical data.")
    @PostMapping("/{structureId}/new-version")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_CREATE')")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> createNewVersion(
            @PathVariable Long structureId,
            @RequestBody(required = false) SalaryStructureCreateRequest overrideRequest) {
        SalaryStructureResponse response = salaryStructureService.createNewVersion(structureId, overrideRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("New salary structure version created successfully in DRAFT status", response));
    }

    // ── STRUCTURE COMPONENTS & CALCULATION RULES ──────────────────────────────

    @Operation(summary = "Add Component to Structure", description = "Maps a salary component to a structure with calculation rules (FIXED, PERCENTAGE, FORMULA).")
    @PostMapping("/{structureId}/components")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_UPDATE')")
    public ResponseEntity<ApiResponse<StructureComponentResponse>> addComponent(
            @PathVariable Long structureId,
            @Valid @RequestBody StructureComponentCreateRequest request) {
        StructureComponentResponse response = salaryStructureComponentService.addComponentToStructure(structureId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary component attached to structure successfully", response));
    }

    @Operation(summary = "Get Structure Components", description = "Retrieves all components and calculation rules attached to a structure sorted by calculation order.")
    @GetMapping("/{structureId}/components")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_VIEW')")
    public ResponseEntity<ApiResponse<List<StructureComponentResponse>>> getComponents(
            @PathVariable Long structureId) {
        List<StructureComponentResponse> list = salaryStructureComponentService.getStructureComponents(structureId);
        return ResponseEntity.ok(ApiResponse.success("Structure components retrieved successfully", list));
    }

    @Operation(summary = "Update Structure Component", description = "Updates calculation rules, base component, percentage, or order of a component in a DRAFT structure.")
    @PutMapping("/{structureId}/components/{structureComponentId}")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_UPDATE')")
    public ResponseEntity<ApiResponse<StructureComponentResponse>> updateComponent(
            @PathVariable Long structureId,
            @PathVariable Long structureComponentId,
            @Valid @RequestBody StructureComponentUpdateRequest request) {
        StructureComponentResponse response = salaryStructureComponentService.updateStructureComponent(structureId, structureComponentId, request);
        return ResponseEntity.ok(ApiResponse.success("Structure component updated successfully", response));
    }

    @Operation(summary = "Remove Component from Structure", description = "Removes a component mapping from a DRAFT salary structure.")
    @DeleteMapping("/{structureId}/components/{structureComponentId}")
    @PreAuthorize("hasAuthority('SALARY_STRUCTURE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> removeComponent(
            @PathVariable Long structureId,
            @PathVariable Long structureComponentId) {
        salaryStructureComponentService.removeComponentFromStructure(structureId, structureComponentId);
        return ResponseEntity.ok(ApiResponse.success("Component removed from structure successfully", null));
    }
}
