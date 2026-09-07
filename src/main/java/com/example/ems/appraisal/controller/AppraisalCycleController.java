package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalCycleResponseDto;
import com.example.ems.appraisal.dto.CreateAppraisalCycleDto;
import com.example.ems.appraisal.service.AppraisalCycleService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appraisals/cycles")
@CrossOrigin("*")
@Tag(name = "Appraisal Cycle Management APIs")
public class AppraisalCycleController {

    @Autowired
    private AppraisalCycleService cycleService;

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

    @Operation(summary = "Create Appraisal Cycle")
    @PostMapping
    public ResponseEntity<?> createCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateAppraisalCycleDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_CREATE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_CREATE permission", "AUTH_002"));
        }

        Employee employee = resolveEmployee(user);
        AppraisalCycleResponseDto created = cycleService.createCycle(dto, employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appraisal cycle created successfully", created));
    }

    @Operation(summary = "Get All Appraisal Cycles")
    @GetMapping
    public ResponseEntity<?> getCycles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<AppraisalCycleResponseDto> cycles = cycleService.getCycles();
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycles retrieved successfully", cycles));
    }

    @Operation(summary = "Get Appraisal Cycle by ID")
    @GetMapping("/{cycleId}")
    public ResponseEntity<?> getCycleById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalCycleResponseDto cycle = cycleService.getCycleById(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycle retrieved successfully", cycle));
    }

    @Operation(summary = "Activate Appraisal Cycle")
    @PostMapping("/{cycleId}/activate")
    public ResponseEntity<?> activateCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_ACTIVATE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_ACTIVATE permission", "AUTH_002"));
        }

        AppraisalCycleResponseDto activated = cycleService.activateCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycle activated successfully", activated));
    }

    @Operation(summary = "Generate Appraisals for Cycle (HR-initiated batch eligibility resolution)")
    @PostMapping("/{cycleId}/generate")
    public ResponseEntity<?> generateAppraisals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_UPDATE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_UPDATE permission", "AUTH_002"));
        }

        Map<String, Object> result = cycleService.generateAppraisalsForCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Appraisals generated successfully for cycle", result));
    }

    @Operation(summary = "Update Appraisal Cycle (Draft only)")
    @PutMapping("/{cycleId}")
    public ResponseEntity<?> updateCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId,
            @Valid @RequestBody com.example.ems.appraisal.dto.UpdateAppraisalCycleDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_UPDATE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_UPDATE permission", "AUTH_002"));
        }

        AppraisalCycleResponseDto updated = cycleService.updateCycle(cycleId, dto);
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycle updated successfully", updated));
    }

    @Operation(summary = "Delete Appraisal Cycle (Draft only)")
    @DeleteMapping("/{cycleId}")
    public ResponseEntity<?> deleteCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_DELETE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_DELETE permission", "AUTH_002"));
        }

        cycleService.deleteCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycle deleted successfully", null));
    }

    @Operation(summary = "Get Cycle Configuration Snapshot")
    @GetMapping("/{cycleId}/configuration")
    public ResponseEntity<?> getCycleConfiguration(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        com.example.ems.appraisal.dto.AppraisalConfigurationSnapshotDto snapshot = cycleService.getCycleConfiguration(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle configuration snapshot retrieved successfully", snapshot));
    }

    @Operation(summary = "Get Cycle Eligibility Preview")
    @GetMapping("/{cycleId}/eligibility-preview")
    public ResponseEntity<?> getEligibilityPreview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        com.example.ems.appraisal.dto.CycleEligibilityPreviewResponseDto preview = cycleService.getEligibilityPreview(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle eligibility preview retrieved successfully", preview));
    }

    @Operation(summary = "Get Cycle Generation Status")
    @GetMapping("/{cycleId}/generation-status")
    public ResponseEntity<?> getGenerationStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        com.example.ems.appraisal.dto.CycleGenerationStatusResponseDto status = cycleService.getGenerationStatus(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle generation status retrieved successfully", status));
    }

    @Operation(summary = "Close Appraisal Cycle")
    @PostMapping("/{cycleId}/close")
    public ResponseEntity<?> closeCycle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_CYCLE_CLOSE") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_CYCLE_CLOSE permission", "AUTH_002"));
        }

        AppraisalCycleResponseDto closed = cycleService.closeCycle(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycle closed successfully", closed));
    }
}

