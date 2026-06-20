package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceExpenseService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/expenses")
@CrossOrigin("*")
@Tag(name = "Expense Management")
public class FinanceExpenseController {

    @Autowired
    private FinanceExpenseService financeExpenseService;

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
        return roleService.hasRoleOrGreater(user, "FINANCE")
                || roleService.hasPermission(user.getWorkEmail(), "reports.finance")
                || roleService.hasPermission(user.getWorkEmail(), "expense.manage");
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires finance privileges.", "AUTH_002"));
    }

    // ── 1. GET DASHBOARD ──────────────────────────────────────────────────────
    @Operation(summary = "Get Expense Dashboard Stats", description = "Retrieves high-level counts of pending, approved, and reimbursed expense claims.")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        ExpenseDashboardResponse data = financeExpenseService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Expense dashboard retrieved successfully", data));
    }

    // ── 2. GET EXPENSE CLAIMS (LIST) ──────────────────────────────────────────
    @Operation(summary = "Get Expense Claims List", description = "Retrieves a paginated and filtered list of all employee expense claims.")
    @GetMapping
    public ResponseEntity<?> getExpenses(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        Pageable pageable = PageRequest.of(page, size);
        FinanceExpenseListResponse data = financeExpenseService.getExpenses(
                status, category, department, search, minAmount, maxAmount, pageable);
        return ResponseEntity.ok(ApiResponse.success("Expense claims list retrieved successfully", data));
    }

    // ── 3. GET EXPENSE DETAILS ───────────────────────────────────────────────
    @Operation(summary = "Get Expense Claim Details", description = "Retrieves full information for a specific expense claim by ID, including audit logs.")
    @GetMapping("/{expenseId}")
    public ResponseEntity<?> getExpenseDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            FinanceExpenseDetailsResponse data = financeExpenseService.getExpenseDetails(expenseId);
            return ResponseEntity.ok(ApiResponse.success("Expense claim details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        }
    }

    // ── 4. VIEW RECEIPT METADATA ─────────────────────────────────────────────
    @Operation(summary = "Get Expense Claim Receipt Metadata", description = "Retrieves attachment and file metadata for the receipt of the specified expense claim.")
    @GetMapping("/{expenseId}/receipt")
    public ResponseEntity<?> getReceiptMetadata(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            FinanceExpenseReceiptResponse data = financeExpenseService.getReceiptMetadata(expenseId);
            return ResponseEntity.ok(ApiResponse.success("Receipt metadata retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        }
    }

    // ── 5. APPROVE CLAIM ─────────────────────────────────────────────────────
    @Operation(summary = "Approve Expense Claim", description = "Approves a pending expense claim.")
    @PatchMapping("/{expenseId}/approve")
    public ResponseEntity<?> approveExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @RequestBody ApproveRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String remarks = request != null && request.getRemarks() != null ? request.getRemarks() : "Verified and approved";
            financeExpenseService.approveExpense(expenseId, remarks, user.getFullName());
            return ResponseEntity.ok(new ExpenseWorkflowResponse("Expense approved successfully", "APPROVED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 6. REJECT CLAIM ──────────────────────────────────────────────────────
    @Operation(summary = "Reject Expense Claim", description = "Rejects a pending expense claim.")
    @PatchMapping("/{expenseId}/reject")
    public ResponseEntity<?> rejectExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @RequestBody RejectRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String reason = request != null && request.getReason() != null ? request.getReason() : "Receipt is invalid";
            financeExpenseService.rejectExpense(expenseId, reason, user.getFullName());
            return ResponseEntity.ok(new ExpenseWorkflowResponse("Expense rejected successfully", "REJECTED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 7. SEND BACK FOR CORRECTION ──────────────────────────────────────────
    @Operation(summary = "Return Expense Claim for Correction", description = "Returns an expense claim back to the employee requesting clarification or missing receipts.")
    @PatchMapping("/{expenseId}/send-back")
    public ResponseEntity<?> sendBackExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @RequestBody SendBackRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String reason = request != null && request.getReason() != null ? request.getReason() : "Upload missing GST invoice";
            financeExpenseService.sendBackExpense(expenseId, reason, user.getFullName());
            return ResponseEntity.ok(new ExpenseWorkflowResponse("Expense returned for correction", "SENT_BACK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 8. REIMBURSE CLAIM ───────────────────────────────────────────────────
    @Operation(summary = "Reimburse Expense Claim", description = "Reimburses an approved expense claim, logging the payment mode and transaction ID.")
    @PatchMapping("/{expenseId}/reimburse")
    public ResponseEntity<?> reimburseExpense(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId,
            @RequestBody ReimburseExpenseRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            if (request == null || request.getPaymentMode() == null || request.getTransactionReference() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Payment mode and transaction reference are required.", "EXP_002"));
            }
            ReimburseExpenseResponse response = financeExpenseService.reimburseExpense(
                    expenseId, request.getPaymentMode(), request.getTransactionReference(), user.getFullName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 9. BULK APPROVE ──────────────────────────────────────────────────────
    @Operation(summary = "Bulk Approve Expense Claims", description = "Approves multiple pending expense claims in a single request.")
    @PatchMapping("/bulk-approve")
    public ResponseEntity<?> bulkApprove(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody BulkExpenseRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            if (request == null || request.getExpenseIds() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Expense IDs are required.", "EXP_002"));
            }
            String remarks = request.getRemarks() != null ? request.getRemarks() : "Bulk approved";
            financeExpenseService.bulkApprove(request.getExpenseIds(), remarks, user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Selected expenses approved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 10. BULK REJECT ──────────────────────────────────────────────────────
    @Operation(summary = "Bulk Reject Expense Claims", description = "Rejects multiple pending expense claims in a single request.")
    @PatchMapping("/bulk-reject")
    public ResponseEntity<?> bulkReject(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody BulkExpenseRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            if (request == null || request.getExpenseIds() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Expense IDs are required.", "EXP_002"));
            }
            String remarks = request.getRemarks() != null ? request.getRemarks() : "Duplicate claims";
            financeExpenseService.bulkReject(request.getExpenseIds(), remarks, user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Selected expenses rejected successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_400"));
        }
    }

    // ── 11. TIMELINE APIs ────────────────────────────────────────────────────
    @Operation(summary = "Get Expense Claim Timeline", description = "Retrieves chronological audit trail logs showing transitions on the specified expense claim.")
    @GetMapping("/{expenseId}/timeline")
    public ResponseEntity<?> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("expenseId") Long expenseId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            List<FinanceExpenseTimelineItem> data = financeExpenseService.getTimeline(expenseId);
            return ResponseEntity.ok(ApiResponse.success("Timeline events retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "EXP_404"));
        }
    }

    // ── 12. REPORTS API (SUMMARY) ────────────────────────────────────────────
    @Operation(summary = "Get Expense Reports Summary", description = "Retrieves consolidated financial stats on expense reports grouped by category and department.")
    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportsSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        ExpenseReportsSummaryResponse data = financeExpenseService.getReportsSummary();
        return ResponseEntity.ok(ApiResponse.success("Expense reports summary retrieved successfully", data));
    }

    // ── 13. EXPORT APIs (CSV / PDF / XLSX) ────────────────────────────────────
    @Operation(summary = "Export Expenses to CSV", description = "Generates and downloads a CSV spreadsheet report of expense claims.")
    @GetMapping("/export/csv")
    public ResponseEntity<?> exportCsv(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        byte[] data = financeExpenseService.exportCsv(status, department, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "expenses-export.csv");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Operation(summary = "Export Expenses to XLSX", description = "Generates and downloads an Excel spreadsheet report of expense claims.")
    @GetMapping("/export/xlsx")
    public ResponseEntity<?> exportXlsx(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        byte[] data = financeExpenseService.exportXlsx(status, department, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "expenses-export.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Operation(summary = "Export Expenses to PDF", description = "Generates and downloads a printable PDF report of expense claims.")
    @GetMapping("/export/pdf")
    public ResponseEntity<?> exportPdf(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        byte[] data = financeExpenseService.exportPdf(status, department, fromDate, toDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "expenses-export.pdf");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // Helper workflow response DTO
    private static class ExpenseWorkflowResponse {
        private String message;
        private String status;

        public ExpenseWorkflowResponse(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
}
