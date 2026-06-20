package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.entity.FinanceOnboardingHistory;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/onboarding")
@CrossOrigin("*")
@Tag(name = "Finance Onboarding")
public class EmployeeFinanceOnboardingController {

    @Autowired
    private EmployeeFinanceOnboardingService service;

    @Autowired
    private UserRepository userRepository;

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

    // ── 1. CREATE FINANCE ONBOARDING ─────────────────────────────────────────
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
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
            EmployeeFinanceOnboarding ob = service.create(body, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employee finance onboarding session initialized successfully", ob));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    // ── 2. GET LIST ──────────────────────────────────────────────────────────
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<EmployeeFinanceOnboarding>>> getList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        List<EmployeeFinanceOnboarding> list = service.list(status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Employee finance onboarding records retrieved successfully", list));
    }

    // ── 3. GET DETAILS ───────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> getDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        return service.get(id)
                .map(ob -> ResponseEntity.ok(ApiResponse.success("Finance onboarding details retrieved", ob)))
                .orElseGet(() -> (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Onboarding session not found with ID: " + id, "ONB_002")));
    }

    // ── 4. UPDATE ────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
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
            EmployeeFinanceOnboarding ob = service.update(id, body, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Finance onboarding details updated successfully", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 5. DELETE ────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> delete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
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
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Draft/Pending onboarding record deleted successfully"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        } catch (IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "VAL_002"));
        }
    }

    // ── 6. VERIFICATION APIS ─────────────────────────────────────────────────
    @PostMapping("/{id}/verify-bank")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> verifyBank(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String status = body.getOrDefault("status", "VERIFIED");
        String notes = body.getOrDefault("notes", "");

        try {
            EmployeeFinanceOnboarding ob = service.verifyBank(id, status, notes, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bank details verification status updated", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PostMapping("/{id}/verify-pan")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> verifyPan(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String status = body.getOrDefault("status", "VERIFIED");
        String notes = body.getOrDefault("notes", "");

        try {
            EmployeeFinanceOnboarding ob = service.verifyPan(id, status, notes, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("PAN details verification status updated", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PostMapping("/{id}/verify-uan")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> verifyUan(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String status = body.getOrDefault("status", "VERIFIED");
        String notes = body.getOrDefault("notes", "");

        try {
            EmployeeFinanceOnboarding ob = service.verifyUan(id, status, notes, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("PF/UAN details verification status updated", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{id}/verification-status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
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
            return ResponseEntity.ok(ApiResponse.success("Verification check statuses retrieved", service.getVerificationStatus(id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    // ── 7. APPROVAL APIS ─────────────────────────────────────────────────────
    @PostMapping("/{id}/approve")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> approve(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String notes = body != null ? body.get("notes") : "Approved by finance manager";

        try {
            EmployeeFinanceOnboarding ob = service.approve(id, user.getWorkEmail(), notes);
            return ResponseEntity.ok(ApiResponse.success("Employee finance onboarding approved successfully", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PostMapping("/{id}/send-back")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> sendBack(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String notes = body.getOrDefault("notes", "Sent back for correction");

        try {
            EmployeeFinanceOnboarding ob = service.sendBack(id, user.getWorkEmail(), notes);
            return ResponseEntity.ok(ApiResponse.success("Finance onboarding profile sent back for correction", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{id}/approval-history")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<FinanceOnboardingHistory>>> getApprovalHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        List<FinanceOnboardingHistory> history = service.getHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Approval and verification action logs retrieved", history));
    }

    // ── 8. SALARY SETUP APIS ─────────────────────────────────────────────────

    @PostMapping("/calculate-ctc")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateCtc(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        BigDecimal monthlyCtc = BigDecimal.ZERO;
        if (body.containsKey("monthlyCtc")) {
            monthlyCtc = new BigDecimal(body.get("monthlyCtc").toString());
        } else if (body.containsKey("ctc")) {
            BigDecimal annualCtc = new BigDecimal(body.get("ctc").toString());
            monthlyCtc = annualCtc.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        } else {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Either ctc (annual) or monthlyCtc must be provided", "VAL_001"));
        }

        return ResponseEntity.ok(ApiResponse.success("CTC breakup calculated successfully", service.calculateCtcBreakup(monthlyCtc)));
    }



    // ── 10. DASHBOARD SUMMARY ────────────────────────────────────────────────
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Finance onboarding summary counts retrieved", service.getDashboardSummary()));
    }

    @GetMapping("/dashboard/recent-activities")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Recent finance onboarding activities retrieved", service.getRecentActivities()));
    }

    // ── 11. REPORTS ──────────────────────────────────────────────────────────
    @GetMapping("/reports")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Finance onboarding report compiled", service.getReportData()));
    }

    @GetMapping("/reports/export")
    public ResponseEntity<String> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "csv") String format) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!checkAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String csv = service.exportReportAsCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=finance_onboarding_report.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }

    // ── 12. ADDED REFINED ENDPOINTS FOR SPEC COMPLIANCE ───────────────────────

    @GetMapping("/pending-reviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<EmployeeFinanceOnboarding>>> getPendingReviews(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        List<EmployeeFinanceOnboarding> list = service.getPendingReviews(department, status);
        return ResponseEntity.ok(ApiResponse.success("Pending finance onboarding reviews retrieved", list));
    }

    @PatchMapping("/{onboardingId}/verify")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> verifyFinancialDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long onboardingId,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        Boolean bankVerified = body.containsKey("bankVerified") ? Boolean.valueOf(body.get("bankVerified").toString()) : Boolean.FALSE;
        Boolean panVerified = body.containsKey("panVerified") ? Boolean.valueOf(body.get("panVerified").toString()) : Boolean.FALSE;
        Boolean uanVerified = body.containsKey("uanVerified") ? Boolean.valueOf(body.get("uanVerified").toString()) : Boolean.FALSE;
        String remarks = body.getOrDefault("remarks", "").toString();

        try {
            EmployeeFinanceOnboarding ob = service.verifyFinancialDetails(onboardingId, bankVerified, panVerified, uanVerified, remarks, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Employee finance onboarding verified successfully", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PatchMapping("/{onboardingId}/reject")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> rejectVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long onboardingId,
            @RequestBody(required = false) Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String notes = body != null ? body.getOrDefault("notes", "Rejected by finance manager") : "Rejected by finance manager";

        try {
            EmployeeFinanceOnboarding ob = service.reject(onboardingId, user.getWorkEmail(), notes);
            return ResponseEntity.ok(ApiResponse.success("Employee finance onboarding rejected", ob));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/bank-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBankDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("bankName", ob.getBankName());
            map.put("bankAccountNumber", ob.getBankAccountNumber());
            map.put("bankIfsc", ob.getBankIfsc());
            map.put("bankVerificationStatus", ob.getBankVerificationStatus());
            map.put("bankVerificationNotes", ob.getBankVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Bank details retrieved", map));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PutMapping("/{employeeId}/bank-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> updateBankDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            body.put("status", ob.getStatus());
            EmployeeFinanceOnboarding updated = service.update(ob.getId(), body, user.getWorkEmail());
            updated.setBankVerificationStatus("PENDING");
            service.verifyBank(updated.getId(), "PENDING", "Details updated by user", user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bank details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PatchMapping("/{employeeId}/bank-details/verify")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> verifyBankAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody(required = false) Map<String, String> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        String remarks = body != null ? body.getOrDefault("remarks", "Bank verified successfully") : "Bank verified successfully";

        try {
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            EmployeeFinanceOnboarding verified = service.verifyBank(ob.getId(), "VERIFIED", remarks, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bank details verified successfully", verified));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/tax-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("panNumber", ob.getPanNumber());
            map.put("panVerificationStatus", ob.getPanVerificationStatus());
            map.put("panVerificationNotes", ob.getPanVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Tax details retrieved", map));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PutMapping("/{employeeId}/tax-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> updateTaxDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            EmployeeFinanceOnboarding updated = service.update(ob.getId(), body, user.getWorkEmail());
            updated.setPanVerificationStatus("PENDING");
            service.verifyPan(updated.getId(), "PENDING", "Details updated by user", user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Tax details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/statutory-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatutoryDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("uanNumber", ob.getUanNumber());
            map.put("uanVerificationStatus", ob.getUanVerificationStatus());
            map.put("uanVerificationNotes", ob.getUanVerificationNotes());
            return ResponseEntity.ok(ApiResponse.success("Statutory details retrieved", map));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PutMapping("/{employeeId}/statutory-details")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> updateStatutoryDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            EmployeeFinanceOnboarding updated = service.update(ob.getId(), body, user.getWorkEmail());
            updated.setUanVerificationStatus("PENDING");
            service.verifyUan(updated.getId(), "PENDING", "Details updated by user", user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Statutory details updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PostMapping("/{employeeId}/salary-structure")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> assignSalaryStructureForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkAccess(user)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
        }

        BigDecimal basic;
        BigDecimal hra;
        BigDecimal allowances;

        if (body.containsKey("salaryStructureId")) {
            basic = BigDecimal.valueOf(50000.00);
            hra = BigDecimal.valueOf(25000.00);
            allowances = BigDecimal.valueOf(10000.00);
        } else {
            basic = new BigDecimal(body.getOrDefault("basicSalary", "0").toString());
            hra = new BigDecimal(body.getOrDefault("hra", "0").toString());
            allowances = new BigDecimal(body.getOrDefault("allowances", "0").toString());
        }

        try {
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            EmployeeFinanceOnboarding updated = service.assignSalaryStructure(ob.getId(), basic, hra, allowances, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Salary structure assigned successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/salary-structure")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalaryStructureForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Assigned salary structure retrieved", service.getSalaryStructure(ob.getId())));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/salary-preview")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalaryPreviewForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Salary breakup and preview generated", service.getSalaryPreview(ob.getId())));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @PostMapping("/{employeeId}/activate-payroll")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EmployeeFinanceOnboarding>> activatePayrollForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            EmployeeFinanceOnboarding activated = service.activatePayroll(ob.getId(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Employee activated for payroll system", activated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }

    @GetMapping("/{employeeId}/payroll-status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayrollStatusForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
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
            EmployeeFinanceOnboarding ob = service.getByEmployeeId(employeeId);
            return ResponseEntity.ok(ApiResponse.success("Payroll activation status checked", service.getPayrollStatus(ob.getId())));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ONB_002"));
        }
    }
}
