package com.example.ems.finance.controller;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.Budget;
import com.example.ems.finance.entity.Vendor;
import com.example.ems.finance.entity.Invoice;
import com.example.ems.finance.service.FinanceService;
import com.example.ems.security.service.JwtService;



import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;



@RestController
@RequestMapping("/api/v1/finance")
@CrossOrigin("*")
@Tag(name = "Finance Setup")
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
    @Operation(summary = "Get Finance Dashboard Stats", description = "Retrieves high-level overview metrics of company expenses, payroll, and settlements for the finance dashboard.")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Finance dashboard statistics retrieved successfully", 
                financeService.getDashboardData()));
    }

    // ── 2. GET MONTHLY ANALYTICS ──────────────────────────────────────────────
    @Operation(summary = "Get Monthly Finance Analytics", description = "Retrieves historical monthly breakdowns of payroll vs general operational expenses.")
    @GetMapping("/analytics/monthly")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyAnalytics(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Monthly finance analytics retrieved successfully", 
                financeService.getMonthlyAnalytics()));
    }

    // ── 3. GET RECENT TRANSACTIONS ────────────────────────────────────────────
    @Operation(summary = "Get Recent Transactions", description = "Retrieves a listing of recent transaction entries including salary disbursement and approved claim audits.")
    @GetMapping("/transactions/recent")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Recent finance transactions retrieved successfully", 
                financeService.getRecentTransactions()));
    }


    // ── 5. GET SALARY SUMMARY ─────────────────────────────────────────────────
    @Operation(summary = "Get Salary Expenses Summary", description = "Retrieves summary metrics of total basic pay, deductions, and net salary payout budgets.")
    @GetMapping("/salary/summary")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalarySummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Payroll/Salary overview summary retrieved successfully", 
                financeService.getSalarySummary()));
    }

    // ── 6. GET PENDING PAYMENTS ───────────────────────────────────────────────
    @Operation(summary = "Get Pending Due Payments", description = "Retrieves a list of pending pay cycles, pending expense claims, and unpaid settlement fees.")
    @GetMapping("/payments/pending")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Pending due payments retrieved successfully", 
                financeService.getPendingPayments()));
    }

    // ── 7. GET CUSTOM REPORT ──────────────────────────────────────────────────
    @Operation(summary = "Generate Custom Finance Report", description = "Generates a custom report of company transactions over a specified date range and type.")
    @GetMapping("/report")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            return ResponseEntity.ok(ApiResponse.success("Custom finance report generated successfully", 
                    financeService.getCustomReport(start, end, type)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Invalid date parameters. Expected format: YYYY-MM-DD", "FIN_001"));
        }
    }

    // ── 8. GET SALARY DISTRIBUTION ──────────────────────────────────────────
    @Operation(summary = "Get Salary Distribution Analytics", description = "Retrieves a distribution categorization listing of employees grouped by salary bracket bands.")
    @GetMapping("/salary/distribution")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSalaryDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Salary distribution retrieved successfully", 
                financeService.getSalaryDistribution()));
    }

    // ── 9. BUDGET CRUD ───────────────────────────────────────────────────────
    @GetMapping("/budgets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getBudgets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        return ResponseEntity.ok(ApiResponse.success("Budgets retrieved successfully", financeService.getAllBudgets()));
    }

    @PostMapping("/budgets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createBudget(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Budget budget) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        return ResponseEntity.ok(ApiResponse.success("Budget created successfully", financeService.createBudget(budget)));
    }

    @PutMapping("/budgets/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateBudget(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Budget budget) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        Optional<Budget> updated = financeService.updateBudget(id, budget);
        if (updated.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Budget not found with ID: " + id, "FIN_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", updated.get()));
    }

    // ── 10. VENDOR CRUD ──────────────────────────────────────────────────────
    @GetMapping("/vendors")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getVendors(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        return ResponseEntity.ok(ApiResponse.success("Vendors retrieved successfully", financeService.getAllVendors()));
    }

    @PostMapping("/vendors")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Vendor vendor) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        return ResponseEntity.ok(ApiResponse.success("Vendor created successfully", financeService.createVendor(vendor)));
    }

    @PutMapping("/vendors/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateVendor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Vendor vendor) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        Optional<Vendor> updated = financeService.updateVendor(id, vendor);
        if (updated.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Vendor not found with ID: " + id, "FIN_003"));
        }
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", updated.get()));
    }

    // ── 11. INVOICE CRUD ─────────────────────────────────────────────────────
    @GetMapping("/invoices")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getInvoices(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", financeService.getAllInvoices()));
    }

    @PostMapping("/invoices")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createInvoice(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Invoice invoice) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        try {
            return ResponseEntity.ok(ApiResponse.success("Invoice created successfully", financeService.createInvoice(invoice)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FIN_004"));
        }
    }

    @PatchMapping("/invoices/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> approveInvoice(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        Optional<Invoice> updated = financeService.approveInvoice(id);
        if (updated.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Invoice not found with ID: " + id, "FIN_005"));
        }
        return ResponseEntity.ok(ApiResponse.success("Invoice approved successfully", updated.get()));
    }

    @PatchMapping("/invoices/{id}/pay")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> payInvoice(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkAccess(user)) return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));

        Optional<Invoice> updated = financeService.payInvoice(id);
        if (updated.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Invoice not found with ID: " + id, "FIN_005"));
        }
        return ResponseEntity.ok(ApiResponse.success("Invoice paid successfully", updated.get()));
    }

}
