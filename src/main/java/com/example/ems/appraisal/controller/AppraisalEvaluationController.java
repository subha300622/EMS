package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.service.AppraisalEvaluationService;
import com.example.ems.appraisal.service.AppraisalHistoryService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
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

@RestController
@RequestMapping("/api/v1/appraisals")
@CrossOrigin("*")
@Tag(name = "Appraisal Evaluation & Review APIs")
public class AppraisalEvaluationController {

    @Autowired
    private AppraisalEvaluationService evaluationService;

    @Autowired
    private AppraisalHistoryService historyService;

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
        if (user == null || user.getWorkEmail() == null) return null;
        return employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
    }

    private boolean hasPermission(User user, String permission) {
        if (user == null) return false;
        if (user.getRole() != null && "PLATFORM_ADMIN".equalsIgnoreCase(user.getRole().getName())) return true;
        return roleService.hasPermission(user.getWorkEmail(), permission);
    }

    @Operation(summary = "Get My Appraisals")
    @GetMapping("/my")
    public ResponseEntity<?> getMyAppraisals(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found", "AUTH_002"));
        }

        List<AppraisalResultResponseDto> list = evaluationService.getEmployeeAppraisals(employee.getId());

        return ResponseEntity.ok(ApiResponse.success("My appraisals retrieved successfully", list));
    }

    @Operation(summary = "Get Appraisal by ID")
    @GetMapping("/{appraisalId}")
    public ResponseEntity<?> getAppraisalById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalResultResponseDto result = evaluationService.getAppraisalResult(appraisalId);
        Employee employee = resolveEmployee(user);
        boolean isOwner = employee != null && employee.getId().equals(result.getEmployeeId());
        boolean hasViewAll = hasPermission(user, "APPRAISAL_VIEW") || hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE");

        if (!isOwner && !hasViewAll) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing permission to view appraisal", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Appraisal retrieved successfully", result));
    }

    @Operation(summary = "Get Employee Appraisal History")
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<?> getEmployeeAppraisals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_VIEW permission", "AUTH_002"));
        }

        List<AppraisalResultResponseDto> list = evaluationService.getEmployeeAppraisals(employeeId);

        return ResponseEntity.ok(ApiResponse.success("Employee appraisals retrieved successfully", list));
    }

    @Operation(summary = "Save Employee Self-Assessment")
    @PostMapping("/{appraisalId}/self-assessment")
    public ResponseEntity<?> saveSelfAssessment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody SelfAssessmentDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found", "AUTH_002"));
        }

        SelfAssessmentDto saved = evaluationService.saveSelfAssessment(appraisalId, dto, employee);
        return ResponseEntity.ok(ApiResponse.success("Self assessment saved successfully", saved));
    }

    @Operation(summary = "Update Employee Self-Assessment")
    @PutMapping("/{appraisalId}/self-assessment")
    public ResponseEntity<?> updateSelfAssessment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody SelfAssessmentDto dto) {
        return saveSelfAssessment(authHeader, appraisalId, dto);
    }

    @Operation(summary = "Submit Employee Self-Assessment")
    @PostMapping("/{appraisalId}/self-assessment/submit")
    public ResponseEntity<?> submitSelfAssessment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody SelfAssessmentDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found", "AUTH_002"));
        }

        SelfAssessmentDto submitted = evaluationService.submitSelfAssessment(appraisalId, dto, employee);
        return ResponseEntity.ok(ApiResponse.success("Self assessment submitted successfully", submitted));
    }

    @Operation(summary = "Submit Reviewer Feedback & Rating for Stage")
    @PostMapping({"/{appraisalId}/review", "/{appraisalId}/reviews"})
    public ResponseEntity<?> submitReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody ReviewStageDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee reviewer = resolveEmployee(user);
        if (reviewer == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Reviewer employee profile not found", "AUTH_002"));
        }

        ReviewStageDto savedReview = evaluationService.submitReview(appraisalId, dto, reviewer, user);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully", savedReview));
    }

    @Operation(summary = "Get Appraisal Result & Ratings")
    @GetMapping("/{appraisalId}/result")
    public ResponseEntity<?> getResult(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalResultResponseDto result = evaluationService.getAppraisalResult(appraisalId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal result retrieved successfully", result));
    }

    @Operation(summary = "Publish Appraisal Result")
    @PostMapping("/{appraisalId}/publish")
    public ResponseEntity<?> publishAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_PUBLISH") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_PUBLISH permission", "AUTH_002"));
        }

        Employee publisher = resolveEmployee(user);
        AppraisalResultResponseDto result = evaluationService.publishAppraisal(appraisalId, publisher);
        return ResponseEntity.ok(ApiResponse.success("Appraisal published successfully", result));
    }

    @Operation(summary = "Get My Appraisal History Logs")
    @GetMapping("/my/history")
    public ResponseEntity<?> getMyHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found", "AUTH_002"));
        }

        List<AppraisalHistoryResponseDto> history = historyService.getHistoryForEmployee(employee.getId());
        return ResponseEntity.ok(ApiResponse.success("My appraisal history retrieved successfully", history));
    }

    @Operation(summary = "Get Employee Appraisal History Logs")
    @GetMapping("/employees/{employeeId}/history")
    public ResponseEntity<?> getEmployeeHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_HISTORY_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_HISTORY_VIEW permission", "AUTH_002"));
        }

        List<AppraisalHistoryResponseDto> history = historyService.getHistoryForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee appraisal history retrieved successfully", history));
    }

    @Operation(summary = "Get Appraisal Audit History by Appraisal ID")
    @GetMapping("/{appraisalId}/history")
    public ResponseEntity<?> getAppraisalHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<AppraisalHistoryResponseDto> history = historyService.getHistoryForAppraisal(appraisalId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal history retrieved successfully", history));
    }
}
