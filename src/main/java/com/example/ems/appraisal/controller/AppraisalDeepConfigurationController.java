package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.service.AppraisalConfigurationExtendedService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
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
@RequestMapping("/api/v1/appraisals/configuration")
@CrossOrigin("*")
@Tag(name = "Appraisal Deep Configuration & Versioning APIs")
public class AppraisalDeepConfigurationController {

    @Autowired
    private AppraisalConfigurationExtendedService extendedService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private Employee resolveEmployee(User user) {
        if (user == null || user.getWorkEmail() == null)
            return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private boolean hasPermission(User user, String permission) {
        if (user == null)
            return false;
        if (user.getRole() != null && "PLATFORM_ADMIN".equalsIgnoreCase(user.getRole().getName()))
            return true;
        return roleService.hasPermission(user.getWorkEmail(), permission);
    }

    // ── 1. CONFIGURATION VALIDATION ENGINE ──────────────────────────────────────

    @Operation(summary = "Validate Appraisal Configuration", description = "Validates stages, criteria weights, rating scale, and performance category intervals proactively")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<AppraisalConfigurationValidationResponseDto>> validateConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_VIEW")
                && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        AppraisalConfigurationValidationResponseDto result = extendedService.validateConfiguration();
        return ResponseEntity.ok(ApiResponse.success("Configuration validation completed", result));
    }

    // ── 2. CONFIGURATION SNAPSHOT & VERSIONING ──────────────────────────────────

    @Operation(summary = "Create Immutable Configuration Version Snapshot")
    @PostMapping("/snapshot")
    public ResponseEntity<ApiResponse<AppraisalConfigurationVersionResponseDto>> createSnapshot(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE", "AUTH_002"));
        }

        Employee employee = resolveEmployee(user);
        String desc = body != null ? body.get("description") : null;
        AppraisalConfigurationVersionResponseDto snapshot = extendedService.createSnapshot(employee, desc);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Configuration version snapshot created successfully", snapshot));
    }

    @Operation(summary = "Get Configuration Version Snapshot by Version Number")
    @GetMapping("/versions/{versionNumber}")
    public ResponseEntity<ApiResponse<AppraisalConfigurationSnapshotDto>> getSnapshotByVersion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer versionNumber) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalConfigurationSnapshotDto snapshot = extendedService.getSnapshotByVersion(versionNumber);
        return ResponseEntity.ok(ApiResponse.success("Configuration snapshot retrieved successfully", snapshot));
    }

    // ── 3. REVIEW STAGES GRANULAR CRUD ──────────────────────────────────────────

    @Operation(summary = "Get Configured Review Stages")
    @GetMapping("/review-stages")
    public ResponseEntity<ApiResponse<List<ReviewStageConfigurationDto>>> getReviewStages(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        List<ReviewStageConfigurationDto> stages = extendedService.getReviewStages();
        return ResponseEntity.ok(ApiResponse.success("Review stages retrieved successfully", stages));
    }

    @Operation(summary = "Create Review Stage")
    @PostMapping("/review-stages")
    public ResponseEntity<ApiResponse<ReviewStageConfigurationDto>> createReviewStage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReviewStageConfigurationDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        ReviewStageConfigurationDto created = extendedService.createReviewStage(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review stage created successfully", created));
    }

    @Operation(summary = "Update Review Stage")
    @PutMapping("/review-stages/{stageId}")
    public ResponseEntity<ApiResponse<ReviewStageConfigurationDto>> updateReviewStage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long stageId,
            @RequestBody ReviewStageConfigurationDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        ReviewStageConfigurationDto updated = extendedService.updateReviewStage(stageId, dto);
        return ResponseEntity.ok(ApiResponse.success("Review stage updated successfully", updated));
    }

    @Operation(summary = "Delete Review Stage")
    @DeleteMapping("/review-stages/{stageId}")
    public ResponseEntity<ApiResponse<Void>> deleteReviewStage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long stageId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        extendedService.deleteReviewStage(stageId);
        return ResponseEntity.ok(ApiResponse.success("Review stage deleted successfully", null));
    }

    @Operation(summary = "Toggle Review Stage Status")
    @PatchMapping("/review-stages/{stageId}/status")
    public ResponseEntity<ApiResponse<ReviewStageConfigurationDto>> toggleReviewStageStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long stageId,
            @RequestBody Map<String, Boolean> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        boolean required = body != null && Boolean.TRUE.equals(body.get("required"));
        ReviewStageConfigurationDto updated = extendedService.updateReviewStageStatus(stageId, required);
        return ResponseEntity.ok(ApiResponse.success("Review stage status updated successfully", updated));
    }

    // ── 4. RATING SCALE & LEVELS CRUD ──────────────────────────────────────────

    @Operation(summary = "Get Rating Scale Configuration")
    @GetMapping("/rating-scale")
    public ResponseEntity<ApiResponse<RatingScaleDto>> getRatingScale(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        RatingScaleDto scale = extendedService.getRatingScale();
        return ResponseEntity.ok(ApiResponse.success("Rating scale retrieved successfully", scale));
    }

    @Operation(summary = "Save or Update Rating Scale")
    @PutMapping("/rating-scale")
    public ResponseEntity<ApiResponse<RatingScaleDto>> saveRatingScale(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RatingScaleDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        RatingScaleDto saved = extendedService.saveOrUpdateRatingScale(dto);
        return ResponseEntity.ok(ApiResponse.success("Rating scale saved successfully", saved));
    }

    // ── 5. CRITERIA & WEIGHTAGE CRUD ──────────────────────────────────────────

    @Operation(summary = "Get Evaluation Criteria")
    @GetMapping("/criteria")
    public ResponseEntity<ApiResponse<List<AppraisalCriterionDto>>> getCriteria(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        List<AppraisalCriterionDto> list = extendedService.getCriteria();
        return ResponseEntity.ok(ApiResponse.success("Evaluation criteria retrieved successfully", list));
    }

    @Operation(summary = "Create Evaluation Criterion")
    @PostMapping("/criteria")
    public ResponseEntity<ApiResponse<AppraisalCriterionDto>> createCriterion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AppraisalCriterionDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        AppraisalCriterionDto created = extendedService.createCriterion(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evaluation criterion created successfully", created));
    }

    @Operation(summary = "Update Evaluation Criterion")
    @PutMapping("/criteria/{criterionId}")
    public ResponseEntity<ApiResponse<AppraisalCriterionDto>> updateCriterion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long criterionId,
            @RequestBody AppraisalCriterionDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        AppraisalCriterionDto updated = extendedService.updateCriterion(criterionId, dto);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion updated successfully", updated));
    }

    @Operation(summary = "Delete Evaluation Criterion")
    @DeleteMapping("/criteria/{criterionId}")
    public ResponseEntity<ApiResponse<Void>> deleteCriterion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long criterionId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        extendedService.deleteCriterion(criterionId);
        return ResponseEntity.ok(ApiResponse.success("Evaluation criterion deleted successfully", null));
    }

    // ── 6. PERFORMANCE CATEGORIES CRUD ──────────────────────────────────────────

    @Operation(summary = "Get Performance Categories")
    @GetMapping("/performance-categories")
    public ResponseEntity<ApiResponse<List<PerformanceCategoryDto>>> getPerformanceCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        List<PerformanceCategoryDto> list = extendedService.getPerformanceCategories();
        return ResponseEntity.ok(ApiResponse.success("Performance categories retrieved successfully", list));
    }

    @Operation(summary = "Create Performance Category")
    @PostMapping("/performance-categories")
    public ResponseEntity<ApiResponse<PerformanceCategoryDto>> createPerformanceCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PerformanceCategoryDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        PerformanceCategoryDto created = extendedService.createPerformanceCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Performance category created successfully", created));
    }

    @Operation(summary = "Update Performance Category")
    @PutMapping("/performance-categories/{categoryId}")
    public ResponseEntity<ApiResponse<PerformanceCategoryDto>> updatePerformanceCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long categoryId,
            @RequestBody PerformanceCategoryDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        PerformanceCategoryDto updated = extendedService.updatePerformanceCategory(categoryId, dto);
        return ResponseEntity.ok(ApiResponse.success("Performance category updated successfully", updated));
    }

    @Operation(summary = "Delete Performance Category")
    @DeleteMapping("/performance-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deletePerformanceCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long categoryId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied", "AUTH_002"));
        }

        extendedService.deletePerformanceCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Performance category deleted successfully", null));
    }
}

