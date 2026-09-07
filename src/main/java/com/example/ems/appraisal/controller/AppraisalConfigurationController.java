package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.service.AppraisalConfigurationService;
import com.example.ems.appraisal.service.AppraisalRequestReasonService;
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
@RequestMapping({"/api/v1/appraisal", "/api/v1/appraisals"})
@CrossOrigin("*")
@Tag(name = "Appraisal Configuration APIs")
public class AppraisalConfigurationController {

    @Autowired
    private AppraisalConfigurationService configurationService;

    @Autowired
    private AppraisalRequestReasonService reasonService;

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

    @Operation(summary = "Get Organization Appraisal Configuration")
    @GetMapping("/configuration")
    public ResponseEntity<?> getConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_VIEW permission", "AUTH_002"));
        }

        AppraisalConfigurationDto dto = configurationService.getConfiguration();
        return ResponseEntity.ok(ApiResponse.success("Appraisal configuration retrieved successfully", dto));
    }

    @Operation(summary = "Save or Update Organization Appraisal Configuration")
    @PutMapping("/configuration")
    public ResponseEntity<?> saveConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody SaveAppraisalConfigurationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE permission", "AUTH_002"));
        }

        Employee employee = resolveEmployee(user);
        try {
            AppraisalConfigurationDto saved = configurationService.saveOrUpdateConfiguration(request, employee);
            return ResponseEntity.ok(ApiResponse.success("Appraisal configuration saved successfully", saved));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error(e.getMessage(), "AUTH_003"));
        }
    }

    @Operation(summary = "Get Appraisal Request Reasons")
    @GetMapping("/request-reasons")
    public ResponseEntity<?> getRequestReasons(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<AppraisalRequestReasonDto> list = reasonService.getReasons(activeOnly);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request reasons retrieved successfully", list));
    }

    @Operation(summary = "Create Appraisal Request Reason")
    @PostMapping("/request-reasons")
    public ResponseEntity<?> createRequestReason(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateRequestReasonDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE permission", "AUTH_002"));
        }

        AppraisalRequestReasonDto created = reasonService.createReason(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appraisal request reason created successfully", created));
    }

    @Operation(summary = "Update Appraisal Request Reason")
    @PutMapping("/request-reasons/{reasonId}")
    public ResponseEntity<?> updateRequestReason(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reasonId,
            @RequestBody UpdateRequestReasonDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE permission", "AUTH_002"));
        }

        AppraisalRequestReasonDto updated = reasonService.updateReason(reasonId, dto);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request reason updated successfully", updated));
    }

    @Operation(summary = "Activate or Deactivate Appraisal Request Reason")
    @PatchMapping("/request-reasons/{reasonId}/status")
    public ResponseEntity<?> updateReasonStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reasonId,
            @RequestBody ReasonStatusDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE permission", "AUTH_002"));
        }

        boolean active = dto.getActive() != null ? dto.getActive() : true;
        AppraisalRequestReasonDto updated = reasonService.updateStatus(reasonId, active);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request reason status updated successfully", updated));
    }

    @Operation(summary = "Delete or Soft-Deactivate Appraisal Request Reason")
    @DeleteMapping("/request-reasons/{reasonId}")
    public ResponseEntity<?> deleteRequestReason(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reasonId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CONFIGURATION_MANAGE permission", "AUTH_002"));
        }

        reasonService.deleteReason(reasonId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request reason removed successfully", null));
    }
}
