package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalRequestResponseDto;
import com.example.ems.appraisal.dto.CreateEmployeeAppraisalRequestDto;
import com.example.ems.appraisal.dto.WithdrawAppraisalRequestDto;
import com.example.ems.appraisal.service.AppraisalRequestService;
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
@RequestMapping({"/api/v1/appraisals/requests", "/api/v1/appraisal/requests"})
@CrossOrigin("*")
@Tag(name = "Employee Appraisal Request APIs")
public class AppraisalRequestController {

    @Autowired
    private AppraisalRequestService requestService;

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

    @Operation(summary = "Create Draft Appraisal Request")
    @PostMapping
    public ResponseEntity<?> createDraftRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateEmployeeAppraisalRequestDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found for authenticated user", "AUTH_002"));
        }

        AppraisalRequestResponseDto created = requestService.createDraftRequest(dto, employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appraisal request draft created successfully", created));
    }

    @Operation(summary = "Submit Appraisal Request")
    @PostMapping("/{requestId}/submit")
    public ResponseEntity<?> submitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found for authenticated user", "AUTH_002"));
        }

        AppraisalRequestResponseDto submitted = requestService.submitRequest(requestId, employee);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request submitted for approval", submitted));
    }

    @Operation(summary = "Get My Appraisal Requests")
    @GetMapping("/my")
    public ResponseEntity<?> getMyRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found for authenticated user", "AUTH_002"));
        }

        List<AppraisalRequestResponseDto> myRequests = requestService.getMyRequests(employee);
        return ResponseEntity.ok(ApiResponse.success("My appraisal requests retrieved successfully", myRequests));
    }

    @Operation(summary = "Get Appraisal Request by ID")
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        AppraisalRequestResponseDto request = requestService.getRequestById(requestId);
        Employee employee = resolveEmployee(user);
        boolean isOwner = employee != null && employee.getId().equals(request.getEmployeeId());
        boolean hasViewAll = hasPermission(user, "APPRAISAL_REQUEST_VIEW") || hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE");

        if (!isOwner && !hasViewAll) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: You do not have permission to view this request", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Appraisal request retrieved successfully", request));
    }

    @Operation(summary = "Withdraw Appraisal Request")
    @PostMapping("/{requestId}/withdraw")
    public ResponseEntity<?> withdrawRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) WithdrawAppraisalRequestDto dto) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found for authenticated user", "AUTH_002"));
        }

        String reason = dto != null ? dto.getReason() : "Withdrawn by employee";
        AppraisalRequestResponseDto withdrawn = requestService.withdrawRequest(requestId, reason, employee);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request withdrawn successfully", withdrawn));
    }

    @Operation(summary = "Resubmit Appraisal Request with Additional Information")
    @PostMapping("/{requestId}/resubmit")
    public ResponseEntity<?> resubmitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Employee profile not found for authenticated user", "AUTH_002"));
        }

        String additionalInfo = body != null ? body.get("additionalInformation") : null;
        AppraisalRequestResponseDto resubmitted = requestService.resubmitRequest(requestId, additionalInfo, employee);
        return ResponseEntity.ok(ApiResponse.success("Appraisal request resubmitted successfully", resubmitted));
    }

    @Operation(summary = "Get All Organization Appraisal Requests")
    @GetMapping
    public ResponseEntity<?> getAllRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_REQUEST_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing APPRAISAL_REQUEST_VIEW permission", "AUTH_002"));
        }

        List<AppraisalRequestResponseDto> all = requestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.success("All appraisal requests retrieved successfully", all));
    }
}
