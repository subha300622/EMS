package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.OnboardingTemplateCreateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateUpdateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateDuplicateRequest;
import com.example.ems.onboarding.dto.OnboardingTemplateResponse;
import com.example.ems.onboarding.service.OnboardingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding/templates")
@CrossOrigin("*")
@Tag(name = "Onboarding Templates")
public class OnboardingTemplateController {

    @Autowired
    private OnboardingTemplateService templateService;

    @PostMapping
    @Operation(summary = "Create Onboarding Template")
    public ResponseEntity<ApiResponse<OnboardingTemplateResponse>> createTemplate(
            @Valid @RequestBody OnboardingTemplateCreateRequest request) {
        OnboardingTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Onboarding template created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get Onboarding Templates List")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getTemplatesList(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search) {
        java.util.Map<String, Object> response = templateService.getTemplatesList(department, status, page, limit, search);
        return ResponseEntity.ok(ApiResponse.success("Templates retrieved successfully", response));
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get Onboarding Template Details")
    public ResponseEntity<ApiResponse<OnboardingTemplateResponse>> getTemplateDetails(
            @PathVariable String templateId) {
        OnboardingTemplateResponse response = templateService.getTemplateDetails(templateId);
        return ResponseEntity.ok(ApiResponse.success("Template details retrieved successfully", response));
    }

    @PatchMapping("/{templateId}")
    @Operation(summary = "Update Onboarding Template")
    public ResponseEntity<ApiResponse<OnboardingTemplateResponse>> updateTemplate(
            @PathVariable String templateId,
            @RequestBody OnboardingTemplateUpdateRequest request) {
        OnboardingTemplateResponse response = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", response));
    }

    @PostMapping("/{templateId}/duplicate")
    @Operation(summary = "Duplicate Onboarding Template")
    public ResponseEntity<ApiResponse<OnboardingTemplateResponse>> duplicateTemplate(
            @PathVariable String templateId,
            @RequestBody OnboardingTemplateDuplicateRequest request) {
        OnboardingTemplateResponse response = templateService.duplicateTemplate(templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template duplicated successfully", response));
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Delete Onboarding Template")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> deleteTemplate(
            @PathVariable String templateId) {
        java.util.Map<String, Object> result = templateService.deleteTemplate(templateId);
        String msg = "deleted".equals(result.get("status")) ? 
                "Template deleted successfully" : "Template archived because it is currently in use";
        return ResponseEntity.ok(ApiResponse.success(msg, result));
    }
}
