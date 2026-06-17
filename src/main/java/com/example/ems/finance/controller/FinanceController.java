package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.service.FinanceService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;



@RestController
@RequestMapping("/api/v1/finance")
@CrossOrigin("*")
@Tag(name = "Finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

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

    // ── 1. GET DASHBOARD ──────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Finance dashboard statistics retrieved successfully", 
                financeService.getDashboardData()));
    }

    // ── 2. GET MONTHLY ANALYTICS ──────────────────────────────────────────────
    @GetMapping("/analytics/monthly")
    public ResponseEntity<?> getMonthlyAnalytics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Monthly finance analytics retrieved successfully", 
                financeService.getMonthlyAnalytics()));
    }

    // ── 3. GET RECENT TRANSACTIONS ────────────────────────────────────────────
    @GetMapping("/transactions/recent")
    public ResponseEntity<?> getRecentTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Recent finance transactions retrieved successfully", 
                financeService.getRecentTransactions()));
    }

    // ── 4. GET EXPENSES BY CATEGORY ───────────────────────────────────────────
    @GetMapping("/expenses/categories")
    public ResponseEntity<?> getExpensesByCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Expense breakdown by category retrieved successfully", 
                financeService.getExpensesByCategory()));
    }

    // ── 5. GET SALARY SUMMARY ─────────────────────────────────────────────────
    @GetMapping("/salary/summary")
    public ResponseEntity<?> getSalarySummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll/Salary overview summary retrieved successfully", 
                financeService.getSalarySummary()));
    }

    // ── 6. GET PENDING PAYMENTS ───────────────────────────────────────────────
    @GetMapping("/payments/pending")
    public ResponseEntity<?> getPendingPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Pending due payments retrieved successfully", 
                financeService.getPendingPayments()));
    }

    // ── 7. GET CUSTOM REPORT ──────────────────────────────────────────────────
    @GetMapping("/report")
    public ResponseEntity<?> getCustomReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            return ResponseEntity.ok(ApiResponse.success("Custom finance report generated successfully", 
                    financeService.getCustomReport(start, end, type)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Invalid date parameters. Expected format: YYYY-MM-DD", "FIN_001"));
        }
    }
}
