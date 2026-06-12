package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.AppraisalCycleResponse;
import com.example.ems.appraisal.dto.AppraisalDashboardResponse;
import com.example.ems.appraisal.dto.AppraisalFinalizeRequest;
import com.example.ems.appraisal.dto.AppraisalManagerReviewRequest;
import com.example.ems.appraisal.dto.AppraisalRequest;
import com.example.ems.appraisal.dto.AppraisalResponse;
import com.example.ems.appraisal.dto.AppraisalSelfReviewRequest;
import com.example.ems.appraisal.dto.IncrementPolicyResponse;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class AppraisalController {

    @Autowired
    private AppraisalService appraisalService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
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

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @GetMapping("/appraisals/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        AppraisalDashboardResponse stats = appraisalService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Appraisal dashboard statistics retrieved successfully", stats));
    }

    // ── 2. APPRAISALS ────────────────────────────────────────────────────────
    @PostMapping("/appraisals")
    public ResponseEntity<?> createAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AppraisalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            AppraisalResponse response = appraisalService.createAppraisal(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Appraisal record created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        }
    }

    @GetMapping("/appraisals")
    public ResponseEntity<?> getAppraisals(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<AppraisalResponse> list;
        if (isFinanceOrManager(currentUser)) {
            list = appraisalService.getAppraisals();
        } else {
            list = currentUser.getEmployeeId() != null
                    ? appraisalService.getAppraisalsByEmployee(Long.parseLong(currentUser.getEmployeeId()))
                    : List.of();
        }
        return ResponseEntity.ok(ApiResponse.success("Appraisals retrieved successfully", list));
    }

    @GetMapping("/appraisals/{id}")
    public ResponseEntity<?> getAppraisalById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<AppraisalResponse> appraisal = appraisalService.getAppraisalById(id);
        if (appraisal.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        AppraisalResponse app = appraisal.get();
        if (!isFinanceOrManager(currentUser) && (currentUser.getEmployeeId() == null
                || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' appraisals.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Appraisal details retrieved successfully", app));
    }

    @PutMapping("/appraisals/{id}")
    public ResponseEntity<?> updateAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            Optional<AppraisalResponse> response = appraisalService.updateAppraisal(id, request);
            if (response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));
            }
            return ResponseEntity.ok(ApiResponse.success("Appraisal updated successfully", response.get()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        }
    }

    @DeleteMapping("/appraisals/{id}")
    public ResponseEntity<?> deleteAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        boolean deleted = appraisalService.deleteAppraisal(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Appraisal record deleted successfully"));
    }

    @PostMapping("/appraisals/{id}/self-review")
    public ResponseEntity<?> submitSelfReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalSelfReviewRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<AppraisalResponse> appOpt = appraisalService.getAppraisalById(id);
        if (appOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        AppraisalResponse app = appOpt.get();
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null
                || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(
                            "Access Denied: You cannot submit self-review for another employee's appraisal.",
                            "AUTH_002"));
        }

        Optional<AppraisalResponse> updated = appraisalService.submitSelfReview(id, request);
        return ResponseEntity.ok(ApiResponse.success("Self-review submitted successfully", updated.get()));
    }

    @PostMapping("/appraisals/{id}/manager-review")
    public ResponseEntity<?> submitManagerReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalManagerReviewRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.submitManagerReview(id, request,
                currentUser.getWorkEmail());
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Manager review submitted successfully", updated.get()));
    }

    @PostMapping("/appraisals/{id}/finalize")
    public ResponseEntity<?> finalizeAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalFinalizeRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.finalizeAppraisal(id, request);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Appraisal finalized successfully", updated.get()));
    }

    @GetMapping("/appraisal-cycles")
    public ResponseEntity<?> getAppraisalCycles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<AppraisalCycleResponse> list = appraisalService.getAppraisalCycles();
        return ResponseEntity.ok(ApiResponse.success("Appraisal cycles retrieved successfully", list));
    }

    @GetMapping("/increment-policies")
    public ResponseEntity<?> getIncrementPolicies(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        List<IncrementPolicyResponse> list = appraisalService.getIncrementPolicies();
        return ResponseEntity.ok(ApiResponse.success("Increment policies retrieved successfully", list));
    }

    // ── 4. REPORTS ───────────────────────────────────────────────────────────
    @GetMapping("/appraisals/reports/{reportType}")
    public ResponseEntity<?> getAppraisalsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        Map<String, Object> data = appraisalService.getAppraisalsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Appraisals report generated successfully", data));
    }

    @GetMapping("/increments/reports/{reportType}")
    public ResponseEntity<?> getIncrementsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        Map<String, Object> data = appraisalService.getIncrementsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Increments report generated successfully", data));
    }

}
