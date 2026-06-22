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
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @Operation(summary = "Get Appraisal Dashboard", tags = {"Appraisals"})
    @GetMapping("/appraisals/dashboard")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        AppraisalDashboardResponse stats = appraisalService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Appraisal dashboard statistics retrieved successfully", stats));
    }

    // ── 2. APPRAISALS ────────────────────────────────────────────────────────
    @Operation(summary = "Create Appraisal Record", tags = {"Appraisals"})
    @PostMapping("/appraisals")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AppraisalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            AppraisalResponse response = appraisalService.createAppraisal(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Appraisal record created successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        }
    }

    @Operation(summary = "Get Appraisals", tags = {"Appraisals"})
    @GetMapping("/appraisals")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAppraisals(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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

    @Operation(summary = "Get Appraisal by ID", tags = {"Appraisals"})
    @GetMapping("/appraisals/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAppraisalById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<AppraisalResponse> appraisal = appraisalService.getAppraisalById(id);
        if (appraisal.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        AppraisalResponse app = appraisal.get();
        if (!isFinanceOrManager(currentUser) && (currentUser.getEmployeeId() == null
                || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' appraisals.",
                            "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Appraisal details retrieved successfully", app));
    }

    @Operation(summary = "Update Appraisal", tags = {"Appraisals"})
    @PutMapping("/appraisals/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            Optional<AppraisalResponse> response = appraisalService.updateAppraisal(id, request);
            if (response.isEmpty()) {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));
            }
            return ResponseEntity.ok(ApiResponse.success("Appraisal updated successfully", response.get()));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "APP_001"));
        }
    }

    @Operation(summary = "Delete Appraisal", tags = {"Appraisals"})
    @DeleteMapping("/appraisals/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        boolean deleted = appraisalService.deleteAppraisal(id);
        if (!deleted) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Appraisal record deleted successfully"));
    }

    @Operation(summary = "Submit Self Review", tags = {"Appraisals"})
    @PostMapping("/appraisals/{id}/self-review")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> submitSelfReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalSelfReviewRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Optional<AppraisalResponse> appOpt = appraisalService.getAppraisalById(id);
        if (appOpt.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        AppraisalResponse app = appOpt.get();
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null
                || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(
                            "Access Denied: You cannot submit self-review for another employee's appraisal.",
                            "AUTH_002"));
        }

        Optional<AppraisalResponse> updated = appraisalService.submitSelfReview(id, request);
        return ResponseEntity.ok(ApiResponse.success("Self-review submitted successfully", updated.get()));
    }

    @Operation(summary = "Submit Manager Review", tags = {"Appraisals"})
    @PostMapping("/appraisals/{id}/manager-review")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> submitManagerReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalManagerReviewRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.submitManagerReview(id, request,
                currentUser.getWorkEmail());
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Manager review submitted successfully", updated.get()));
    }

    @Operation(summary = "Finalize Appraisal", tags = {"Appraisals"})
    @PostMapping("/appraisals/{id}/finalize")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> finalizeAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AppraisalFinalizeRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.finalizeAppraisal(id, request);
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Appraisal finalized successfully", updated.get()));
    }

    @Operation(summary = "Approve Appraisal", tags = {"Appraisals"})
    @PatchMapping("/appraisals/{id}/approve")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> approveAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.approveAppraisal(id);
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Appraisal approved successfully", updated.get()));
    }

    @Operation(summary = "Reject Appraisal", tags = {"Appraisals"})
    @PatchMapping("/appraisals/{id}/reject")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> rejectAppraisal(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<AppraisalResponse> updated = appraisalService.rejectAppraisal(id);
        if (updated.isEmpty())
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Appraisal not found with ID: " + id, "APP_002"));

        return ResponseEntity.ok(ApiResponse.success("Appraisal rejected successfully", updated.get()));
    }

    @Operation(summary = "Get Appraisal Cycles", tags = {"Performance Cycles"})
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

    @Operation(summary = "Get Increment Policies", tags = {"Increment Policies"})
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

    // ── 4. REPORTS ───────────────────────────────────────────────────────────
    @Operation(summary = "Get Appraisals Report", tags = {"Appraisals"})
    @GetMapping("/appraisals/reports/{reportType}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAppraisalsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isFinanceOrManager(currentUser))
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager/Finance permissions.", "AUTH_002"));

        Map<String, Object> data = appraisalService.getAppraisalsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Appraisals report generated successfully", data));
    }

    @Operation(summary = "Get Increments Report", tags = {"Increment Policies"})
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
