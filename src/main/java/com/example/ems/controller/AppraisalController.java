package com.example.ems.controller;

import com.example.ems.dto.*;
import com.example.ems.entity.Employee;
import com.example.ems.entity.User;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.AppraisalService;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AppraisalController {

    @Autowired private AppraisalService appraisalService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private JwtService jwtService;
    @Autowired private RoleService roleService;

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

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @GetMapping("/appraisals/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

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
        if (isManager(currentUser)) {
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
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view other employees' appraisals.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Appraisal details retrieved successfully", app));
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
        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(String.valueOf(app.getEmployeeId())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot submit self-review for another employee's appraisal.", "AUTH_002"));
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

        Optional<AppraisalResponse> updated = appraisalService.submitManagerReview(id, request, currentUser.getWorkEmail());
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

    // ── 3. INCREMENTS ────────────────────────────────────────────────────────
    @PostMapping("/increments")
    public ResponseEntity<?> createIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody IncrementRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        try {
            IncrementResponse response = appraisalService.createIncrement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Increment request submitted successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INC_001"));
        }
    }

    @PatchMapping("/increments/{id}/approve")
    public ResponseEntity<?> approveIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<IncrementResponse> updated = appraisalService.approveIncrement(id, currentUser.getWorkEmail());
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));

        return ResponseEntity.ok(ApiResponse.success("Increment request approved successfully", updated.get()));
    }

    @PostMapping("/increments/{id}/apply")
    public ResponseEntity<?> applyIncrement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Optional<IncrementResponse> updated = appraisalService.applyIncrement(id);
        if (updated.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Increment request not found with ID: " + id, "INC_002"));

        return ResponseEntity.ok(ApiResponse.success("Increment applied and employee compensation updated successfully", updated.get()));
    }

    @GetMapping("/employees/{id}/salary-revisions")
    public ResponseEntity<?> getSalaryRevisions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (!isManager(currentUser) && (currentUser.getEmployeeId() == null || !currentUser.getEmployeeId().equals(String.valueOf(id)))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: You cannot view salary revisions of other employees.", "AUTH_002"));
        }

        List<SalaryRevisionResponse> list = appraisalService.getSalaryRevisions(id);
        return ResponseEntity.ok(ApiResponse.success("Salary revisions history retrieved successfully", list));
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
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

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
        if (!isManager(currentUser))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires HR/Manager permissions.", "AUTH_002"));

        Map<String, Object> data = appraisalService.getIncrementsReport(reportType);
        return ResponseEntity.ok(ApiResponse.success("Increments report generated successfully", data));
    }

    // ── 5. LETTER ────────────────────────────────────────────────────────────
    @GetMapping("/increments/{id}/letter")
    public ResponseEntity<?> getIncrementLetter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            IncrementLetterResponse letter = appraisalService.getIncrementLetter(id);

            // Access check: allow if manager, or if letter belongs to employee
            if (!isManager(currentUser)) {
                // Find target employee
                Optional<Employee> empOpt = employeeRepository.findByEmail(currentUser.getWorkEmail());
                if (empOpt.isEmpty() || !empOpt.get().getFullName().equals(letter.getEmployeeName())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ErrorResponse.error("Access Denied: You cannot view other employees' letters.", "AUTH_002"));
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Increment revision letter generated successfully", letter));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "INC_002"));
        }
    }
}
