package com.example.ems.offboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import com.example.ems.offboarding.service.OffboardingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/offboarding/templates", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Offboarding Template Management", description = "Endpoints for configuring organization Offboarding Templates, clearance checklists, asset return policies, document handover requirements, knowledge transfer, and exit interviews.")
public class OffboardingTemplateController {

    private final OffboardingTemplateService templateService;

    @Autowired
    public OffboardingTemplateController(OffboardingTemplateService templateService) {
        this.templateService = templateService;
    }

    // ==========================================
    // 1. Base Offboarding Template Endpoints
    // ==========================================

    @Operation(summary = "Create Offboarding Template", description = "Creates a new offboarding master template in DRAFT status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Template created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateResponse>> createTemplate(
            @Valid @RequestBody OffboardingTemplateRequest request) {
        OffboardingTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offboarding template created successfully", response));
    }

    @Operation(summary = "Update Offboarding Template", description = "Updates an existing offboarding master template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateResponse>> updateTemplate(
            @PathVariable("id") Long id,
            @Valid @RequestBody OffboardingTemplateRequest request) {
        OffboardingTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template updated successfully", response));
    }

    @Operation(summary = "Get Offboarding Template by ID", description = "Retrieves an offboarding master template summary by ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateResponse>> getTemplate(@PathVariable("id") Long id) {
        OffboardingTemplateResponse response = templateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template retrieved successfully", response));
    }

    @Operation(summary = "Get Complete Offboarding Template Details", description = "Retrieves full offboarding template hierarchy including tasks, asset requirements, document requirements, KT, and exit interviews.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateDetailResponse>> getTemplateDetails(@PathVariable("id") Long id) {
        OffboardingTemplateDetailResponse response = templateService.getTemplateDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template details retrieved successfully", response));
    }

    @Operation(summary = "List Offboarding Templates", description = "Retrieves paginated list of organization offboarding templates with optional status and name search filters.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OffboardingTemplateResponse>>> listTemplates(
            @RequestParam(value = "status", required = false) OffboardingTemplateStatus status,
            @RequestParam(value = "search", required = false) String search,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OffboardingTemplateResponse> response = templateService.listTemplates(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Offboarding templates retrieved successfully", response));
    }

    @Operation(summary = "Archive Offboarding Template", description = "Transitions an offboarding template to ARCHIVED status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template archived successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable("id") Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template archived successfully", null));
    }

    @Operation(summary = "Activate Offboarding Template", description = "Transitions a valid offboarding template from DRAFT or INACTIVE to ACTIVE status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Incomplete template configuration", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lifecycle conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateResponse>> activateTemplate(@PathVariable("id") Long id) {
        OffboardingTemplateResponse response = templateService.activateTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template activated successfully", response));
    }

    @Operation(summary = "Deactivate Offboarding Template", description = "Transitions an active offboarding template to INACTIVE status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lifecycle conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateResponse>> deactivateTemplate(@PathVariable("id") Long id) {
        OffboardingTemplateResponse response = templateService.deactivateTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Offboarding template deactivated successfully", response));
    }

    @Operation(summary = "Clone Offboarding Template", description = "Creates a complete deep clone copy of an existing offboarding template in DRAFT status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Template cloned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/clone")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingTemplateDetailResponse>> cloneTemplate(
            @PathVariable("id") Long id,
            @RequestParam(value = "name", required = false) String name) {
        OffboardingTemplateDetailResponse response = templateService.cloneTemplate(id, name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offboarding template cloned successfully", response));
    }

    // ==========================================
    // 2. Clearance Task Template Endpoints
    // ==========================================

    @Operation(summary = "Add Clearance Task Template", description = "Adds a clearance checklist item to an offboarding master template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Clearance task added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/clearance-tasks", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClearanceTaskTemplateResponse>> addClearanceTask(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody ClearanceTaskTemplateRequest request) {
        ClearanceTaskTemplateResponse response = templateService.addClearanceTask(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clearance task template added successfully", response));
    }

    @Operation(summary = "Update Clearance Task Template", description = "Updates an existing clearance checklist item in a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance task updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{templateId}/clearance-tasks/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClearanceTaskTemplateResponse>> updateClearanceTask(
            @PathVariable("templateId") Long templateId,
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody ClearanceTaskTemplateRequest request) {
        ClearanceTaskTemplateResponse response = templateService.updateClearanceTask(templateId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Clearance task template updated successfully", response));
    }

    @Operation(summary = "Delete Clearance Task Template", description = "Deletes a clearance task item from a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance task deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{templateId}/clearance-tasks/{taskId}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClearanceTask(
            @PathVariable("templateId") Long templateId,
            @PathVariable("taskId") Long taskId) {
        templateService.deleteClearanceTask(templateId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Clearance task template deleted successfully", null));
    }

    @Operation(summary = "List Clearance Task Templates", description = "Retrieves all clearance task items configured under a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance tasks retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{templateId}/clearance-tasks")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ClearanceTaskTemplateResponse>>> listClearanceTasks(
            @PathVariable("templateId") Long templateId) {
        List<ClearanceTaskTemplateResponse> response = templateService.listClearanceTasks(templateId);
        return ResponseEntity.ok(ApiResponse.success("Clearance task templates retrieved successfully", response));
    }

    // ==========================================
    // 3. Asset Requirement Template Endpoints
    // ==========================================

    @Operation(summary = "Add Asset Requirement Template", description = "Adds an asset return and verification rule to a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Asset requirement added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/asset-requirements", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetRequirementTemplateResponse>> addAssetRequirement(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody AssetRequirementTemplateRequest request) {
        AssetRequirementTemplateResponse response = templateService.addAssetRequirement(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset requirement template added successfully", response));
    }

    @Operation(summary = "Update Asset Requirement Template", description = "Updates an existing asset return requirement in a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset requirement updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset requirement not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{templateId}/asset-requirements/{assetReqId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetRequirementTemplateResponse>> updateAssetRequirement(
            @PathVariable("templateId") Long templateId,
            @PathVariable("assetReqId") Long assetReqId,
            @Valid @RequestBody AssetRequirementTemplateRequest request) {
        AssetRequirementTemplateResponse response = templateService.updateAssetRequirement(templateId, assetReqId, request);
        return ResponseEntity.ok(ApiResponse.success("Asset requirement template updated successfully", response));
    }

    @Operation(summary = "Delete Asset Requirement Template", description = "Deletes an asset return requirement from a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset requirement deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Asset requirement not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{templateId}/asset-requirements/{assetReqId}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssetRequirement(
            @PathVariable("templateId") Long templateId,
            @PathVariable("assetReqId") Long assetReqId) {
        templateService.deleteAssetRequirement(templateId, assetReqId);
        return ResponseEntity.ok(ApiResponse.success("Asset requirement template deleted successfully", null));
    }

    @Operation(summary = "List Asset Requirement Templates", description = "Retrieves all asset requirements configured for a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Asset requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{templateId}/asset-requirements")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AssetRequirementTemplateResponse>>> listAssetRequirements(
            @PathVariable("templateId") Long templateId) {
        List<AssetRequirementTemplateResponse> response = templateService.listAssetRequirements(templateId);
        return ResponseEntity.ok(ApiResponse.success("Asset requirement templates retrieved successfully", response));
    }

    // ==========================================
    // 4. Document Requirement Template Endpoints
    // ==========================================

    @Operation(summary = "Add Document Requirement Template", description = "Adds a document signing or handover requirement to a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document requirement added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/document-requirements", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentRequirementTemplateResponse>> addDocumentRequirement(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody DocumentRequirementTemplateRequest request) {
        DocumentRequirementTemplateResponse response = templateService.addDocumentRequirement(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document requirement template added successfully", response));
    }

    @Operation(summary = "Update Document Requirement Template", description = "Updates an existing document requirement in a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document requirement updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document requirement not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{templateId}/document-requirements/{docReqId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentRequirementTemplateResponse>> updateDocumentRequirement(
            @PathVariable("templateId") Long templateId,
            @PathVariable("docReqId") Long docReqId,
            @Valid @RequestBody DocumentRequirementTemplateRequest request) {
        DocumentRequirementTemplateResponse response = templateService.updateDocumentRequirement(templateId, docReqId, request);
        return ResponseEntity.ok(ApiResponse.success("Document requirement template updated successfully", response));
    }

    @Operation(summary = "Delete Document Requirement Template", description = "Deletes a document requirement from a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document requirement deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document requirement not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{templateId}/document-requirements/{docReqId}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentRequirement(
            @PathVariable("templateId") Long templateId,
            @PathVariable("docReqId") Long docReqId) {
        templateService.deleteDocumentRequirement(templateId, docReqId);
        return ResponseEntity.ok(ApiResponse.success("Document requirement template deleted successfully", null));
    }

    @Operation(summary = "List Document Requirement Templates", description = "Retrieves all document requirements configured for a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{templateId}/document-requirements")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentRequirementTemplateResponse>>> listDocumentRequirements(
            @PathVariable("templateId") Long templateId) {
        List<DocumentRequirementTemplateResponse> response = templateService.listDocumentRequirements(templateId);
        return ResponseEntity.ok(ApiResponse.success("Document requirement templates retrieved successfully", response));
    }

    // ==========================================
    // 5. Knowledge Transfer (KT) Endpoints
    // ==========================================

    @Operation(summary = "Configure KT Requirement Template", description = "Saves or updates Knowledge Transfer requirement guidelines in a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KT configured successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/kt-requirements", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<KtTemplateResponse>> configureKtTemplate(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody KtTemplateRequest request) {
        KtTemplateResponse response = templateService.configureKtTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success("Knowledge transfer requirement configured successfully", response));
    }

    @Operation(summary = "Get KT Requirement Template", description = "Retrieves Knowledge Transfer requirement configuration for a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KT configuration retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template or KT not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{templateId}/kt-requirements")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KtTemplateResponse>> getKtTemplate(@PathVariable("templateId") Long templateId) {
        KtTemplateResponse response = templateService.getKtTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge transfer requirement retrieved successfully", response));
    }

    @Operation(summary = "Delete KT Requirement Template", description = "Deletes Knowledge Transfer requirement configuration from a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "KT configuration deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{templateId}/kt-requirements")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteKtTemplate(@PathVariable("templateId") Long templateId) {
        templateService.deleteKtTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Knowledge transfer requirement deleted successfully", null));
    }

    // ==========================================
    // 6. Exit Interview & Question Endpoints
    // ==========================================

    @Operation(summary = "Configure Exit Interview Template", description = "Saves or updates Exit Interview configuration in a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit interview configured successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/exit-interview", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InterviewTemplateResponse>> configureInterviewTemplate(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody InterviewTemplateRequest request) {
        InterviewTemplateResponse response = templateService.configureInterviewTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success("Exit interview configuration saved successfully", response));
    }

    @Operation(summary = "Get Exit Interview Template", description = "Retrieves Exit Interview configuration and questions for a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit interview configuration retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template or interview not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{templateId}/exit-interview")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InterviewTemplateResponse>> getInterviewTemplate(@PathVariable("templateId") Long templateId) {
        InterviewTemplateResponse response = templateService.getInterviewTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Exit interview configuration retrieved successfully", response));
    }

    @Operation(summary = "Add Exit Interview Question", description = "Adds a question to the Exit Interview configuration of a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Interview question added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{templateId}/exit-interview/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> addInterviewQuestion(
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody InterviewQuestionRequest request) {
        InterviewQuestionResponse response = templateService.addInterviewQuestion(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interview question added successfully", response));
    }

    @Operation(summary = "Update Exit Interview Question", description = "Updates an existing question in the Exit Interview template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interview question updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{templateId}/exit-interview/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> updateInterviewQuestion(
            @PathVariable("templateId") Long templateId,
            @PathVariable("questionId") Long questionId,
            @Valid @RequestBody InterviewQuestionRequest request) {
        InterviewQuestionResponse response = templateService.updateInterviewQuestion(templateId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success("Interview question updated successfully", response));
    }

    @Operation(summary = "Delete Exit Interview Question", description = "Deletes an interview question from a template.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interview question deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{templateId}/exit-interview/questions/{questionId}")
    @PreAuthorize("hasAuthority('OFFBOARDING_TEMPLATE_MANAGE') or hasRole('HR') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInterviewQuestion(
            @PathVariable("templateId") Long templateId,
            @PathVariable("questionId") Long questionId) {
        templateService.deleteInterviewQuestion(templateId, questionId);
        return ResponseEntity.ok(ApiResponse.success("Interview question deleted successfully", null));
    }
}
