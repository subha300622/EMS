package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalCycleDashboardDto;
import com.example.ems.appraisal.dto.AppraisalEmployeeDashboardDto;
import com.example.ems.appraisal.dto.AppraisalOrganizationDashboardDto;
import com.example.ems.appraisal.service.AppraisalDashboardService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appraisals")
@CrossOrigin("*")
@Tag(name = "Appraisal Dashboards & Analytics APIs")
public class AppraisalDashboardController {

    @Autowired
    private AppraisalDashboardService dashboardService;

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

    @Operation(summary = "Get Organization-wide Appraisal Dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AppraisalOrganizationDashboardDto>> getOrganizationDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied: Missing APPRAISAL_VIEW permission", "AUTH_002"));
        }

        AppraisalOrganizationDashboardDto dashboard = dashboardService.getOrganizationDashboard();
        return ResponseEntity.ok(ApiResponse.success("Organization appraisal dashboard retrieved successfully", dashboard));
    }

    @Operation(summary = "Get Appraisal Cycle-specific Dashboard")
    @GetMapping("/cycles/{cycleId}/dashboard")
    public ResponseEntity<ApiResponse<AppraisalCycleDashboardDto>> getCycleDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long cycleId) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasPermission(user, "APPRAISAL_VIEW") && !hasPermission(user, "APPRAISAL_CONFIGURATION_MANAGE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access Denied: Missing APPRAISAL_VIEW permission", "AUTH_002"));
        }

        AppraisalCycleDashboardDto dashboard = dashboardService.getCycleDashboard(cycleId);
        return ResponseEntity.ok(ApiResponse.success("Cycle appraisal dashboard retrieved successfully", dashboard));
    }

    @Operation(summary = "Get Employee Personal Appraisal Dashboard")
    @GetMapping("/my/dashboard")
    public ResponseEntity<ApiResponse<AppraisalEmployeeDashboardDto>> getEmployeeDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        Employee employee = resolveEmployee(user);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Employee profile not found", "AUTH_002"));
        }

        AppraisalEmployeeDashboardDto dashboard = dashboardService.getEmployeeDashboard(employee);
        return ResponseEntity.ok(ApiResponse.success("Employee appraisal dashboard retrieved successfully", dashboard));
    }
}
