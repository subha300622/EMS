package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.service.AppraisalIncrementService;
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
@Tag(name = "Appraisal Increment Management APIs")
public class AppraisalIncrementController {

    @Autowired
    private AppraisalIncrementService incrementService;

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

    @Operation(summary = "Calculate and Preview Appraisal Increment Proposal")
    @GetMapping("/{appraisalId}/increment/preview")
    public ResponseEntity<?> previewIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_INCREMENT_VIEW") && !hasPermission(user, "APPRAISAL_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing permission to view increment proposal", "AUTH_002"));
        }

        IncrementCalculationPreviewDto preview = incrementService.calculateIncrementPreview(appraisalId);
        return ResponseEntity.ok(ApiResponse.success("Increment calculation preview generated successfully", preview));
    }

    @Operation(summary = "Approve Appraisal Increment Proposal")
    @PostMapping("/{appraisalId}/increment/approve")
    public ResponseEntity<?> approveIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody ApproveIncrementRequestDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_INCREMENT_APPROVE") && !hasPermission(user, "APPRAISAL_APPROVE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_INCREMENT_APPROVE permission", "AUTH_002"));
        }

        Employee approver = resolveEmployee(user);
        AppraisalIncrementResponseDto result = incrementService.approveIncrement(appraisalId, dto, approver);
        return ResponseEntity.ok(ApiResponse.success("Appraisal increment approved successfully", result));
    }

    @Operation(summary = "Apply Approved Appraisal Increment to Payroll")
    @PostMapping("/{appraisalId}/increment/apply")
    public ResponseEntity<?> applyIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId,
            @Valid @RequestBody(required = false) ApplyIncrementRequestDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_INCREMENT_APPLY") && !hasPermission(user, "SALARY_MANAGE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_INCREMENT_APPLY permission", "AUTH_002"));
        }

        Employee actor = resolveEmployee(user);
        ApplyIncrementRequestDto req = dto != null ? dto : new ApplyIncrementRequestDto();
        AppraisalIncrementResponseDto result = incrementService.applyIncrement(appraisalId, req, actor);
        return ResponseEntity.ok(ApiResponse.success("Increment applied to payroll successfully", result));
    }

    @Operation(summary = "Get Appraisal Increment Details")
    @GetMapping("/{appraisalId}/increment")
    public ResponseEntity<?> getIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long appraisalId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalIncrementResponseDto result = incrementService.getIncrementByAppraisalId(appraisalId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal increment retrieved successfully", result));
    }

    @Operation(summary = "Get Appraisal Increment History")
    @GetMapping("/increments/history")
    public ResponseEntity<?> getIncrementHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_INCREMENT_VIEW") && !hasPermission(user, "APPRAISAL_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_INCREMENT_VIEW permission", "AUTH_002"));
        }

        List<AppraisalIncrementResponseDto> history = incrementService.getIncrementHistory(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Increment history retrieved successfully", history));
    }
}
