package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.FinanceOnboarding;
import com.example.ems.finance.service.FinanceOnboardingService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Finance")
public class FinanceOnboardingController {

    @Autowired
    private FinanceOnboardingService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

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

    private boolean checkAccess(User user) {
        if (user == null) {
            return false;
        }
        if (roleService.hasRoleOrGreater(user, "FINANCE")) {
            return true;
        }
        return roleService.hasPermission(user.getWorkEmail(), "reports.finance")
                || roleService.hasPermission(user.getWorkEmail(), "expense.manage");
    }

    // ── 1. CREATE ONBOARDING ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        FinanceOnboarding onboarding = service.createOnboarding();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Finance onboarding process started successfully", onboarding));
    }

    // ── 2. RESUME CURRENT ONBOARDING ────────────────────────────────────────
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        return service.getCurrentOnboarding()
                .<ResponseEntity<?>>map(ob -> ResponseEntity.ok(ApiResponse.success("Current finance onboarding retrieved", ob)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("No active onboarding session found", "OB_001")));
    }

    // ── 3. GET DETAILS ──────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getOnboardingDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        return service.getOnboardingById(id)
                .<ResponseEntity<?>>map(ob -> ResponseEntity.ok(ApiResponse.success("Finance onboarding details retrieved", ob)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Onboarding session not found with ID: " + id, "OB_002")));
    }

    // ── 4. GET PROGRESS ─────────────────────────────────────────────────────
    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getOnboardingProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        return service.getOnboardingById(id)
                .<ResponseEntity<?>>map(ob -> {
                    int pct = service.calculateProgress(ob);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding progress calculated", 
                            Map.of("id", id, "progressPercentage", pct, "status", ob.getStatus())));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Onboarding session not found with ID: " + id, "OB_002")));
    }

    // ── 5. STEP PATCHES ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/company")
    public ResponseEntity<?> updateCompany(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updateCompany(id, data);
            return ResponseEntity.ok(ApiResponse.success("Company settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/bank-account")
    public ResponseEntity<?> updateBankAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updateBankAccount(id, data);
            return ResponseEntity.ok(ApiResponse.success("Bank account settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/tax")
    public ResponseEntity<?> updateTax(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updateTax(id, data);
            return ResponseEntity.ok(ApiResponse.success("Tax settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/payment-method")
    public ResponseEntity<?> updatePaymentMethod(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updatePaymentMethod(id, data);
            return ResponseEntity.ok(ApiResponse.success("Payment method settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/payroll")
    public ResponseEntity<?> updatePayroll(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updatePayroll(id, data);
            return ResponseEntity.ok(ApiResponse.success("Payroll settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    @PatchMapping("/{id}/budget")
    public ResponseEntity<?> updateBudget(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding updated = service.updateBudget(id, data);
            return ResponseEntity.ok(ApiResponse.success("Budget settings step updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_003"));
        }
    }

    // ── 6. VALIDATE ONBOARDING ──────────────────────────────────────────────
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            List<String> errors = service.validateOnboarding(id);
            if (errors.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Finance onboarding successfully validated. All configurations are correct."));
            } else {
                return ResponseEntity.badRequest().body(ErrorResponse.error("Validation failed: " + String.join(", ", errors), "OB_004"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OB_002"));
        }
    }

    // ── 7. COMPLETE ONBOARDING ──────────────────────────────────────────────
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance onboarding privileges.", "AUTH_002"));
        }

        try {
            FinanceOnboarding completed = service.completeOnboarding(id);
            return ResponseEntity.ok(ApiResponse.success("Finance onboarding completed and system-wide settings updated successfully", completed));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OB_004"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "OB_002"));
        }
    }

    // ── 8. ADMIN LIST & ARCHIVE ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> listOnboardingRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires admin/finance privileges.", "AUTH_002"));
        }

        List<FinanceOnboarding> list = service.listAll();
        return ResponseEntity.ok(ApiResponse.success("All finance onboarding records retrieved successfully", list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> archiveOnboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires admin/finance privileges.", "AUTH_002"));
        }

        boolean archived = service.archiveOnboarding(id);
        if (archived) {
            return ResponseEntity.ok(ApiResponse.success("Finance onboarding record archived successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Onboarding session not found with ID: " + id, "OB_002"));
        }
    }
}
