package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalCycleResponse;
import com.example.ems.appraisal.dto.IncrementPolicyResponse;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/performance")
@CrossOrigin("*")
public class AppraisalController {

    @Autowired
    private AppraisalService appraisalService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private RoleService roleService;

    // ── Auth helpers ─────────────────────────────────────────────────────────
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

    private boolean isManager(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "employee.update")
                || roleService.hasPermission(user.getWorkEmail(), "employee.delete")
                || roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    private boolean isFinanceOrManager(User user) {
        if (user == null) {
            return false;
        }
        if (isManager(user)) {
            return true;
        }

        // Control access by Role ID hierarchy
        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }

        // Accept the permission check
        return roleService.hasPermission(user.getWorkEmail(), "salary.manage")
                || roleService.hasPermission(user.getWorkEmail(), "payroll.manage")
                || roleService.hasPermission(user.getWorkEmail(), "reports.finance");
    }

    // ── PERFORMANCE CYCLES & POLICIES ───────────────────────────────────────
    @Operation(summary = "Get Appraisal Cycles", tags = { "Performance Cycles" })
    @GetMapping("/appraisal-cycles")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAppraisalCycles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<AppraisalCycleResponse> list = appraisalService.getAppraisalCycles();
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycles retrieved successfully", list));
    }

    @Operation(summary = "Get Increment Policies", tags = { "Increment Policies" })
    @GetMapping("/increment-policies")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getIncrementPolicies(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<IncrementPolicyResponse> list = appraisalService.getIncrementPolicies();
        return ResponseEntity.ok(ApiResponse.success("Increment policies retrieved successfully", list));
    }

    @Operation(summary = "Get Increments Report", tags = { "Increment Policies" })
    @GetMapping("/increments/reports/{reportType}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getIncrementsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        Map<String, Object> data = appraisalService.getIncrementsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Increments report generated successfully", data));
    }
}
