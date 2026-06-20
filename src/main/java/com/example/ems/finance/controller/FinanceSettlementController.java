package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceSettlementService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/settlements")
@CrossOrigin("*")
@Tag(name = "Finance Settlement Management")
public class FinanceSettlementController {

    @Autowired
    private FinanceSettlementService settlementService;

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
        return roleService.hasPermission(user.getWorkEmail(), "fnf.manage");
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
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        SettlementDashboardResponse data = settlementService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Settlement dashboard retrieved successfully", data));
    }

    // ── 2. GET SETTLEMENTS LIST ───────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getSettlements(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        SettlementListResponse data = settlementService.getSettlements(status, search, department, page, size);
        return ResponseEntity.ok(ApiResponse.success("Settlements list retrieved successfully", data));
    }

    // ── 3. GET SETTLEMENT BY ID (REVIEW POPUP) ────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getSettlement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            SettlementReviewResponse data = settlementService.getReviewPopup(id);
            return ResponseEntity.ok(ApiResponse.success("Settlement details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 4. GET ASSET CLEARANCE ────────────────────────────────────────────────
    @GetMapping("/{id}/asset-clearance")
    public ResponseEntity<?> getAssetClearance(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            AssetRecoveryResponse data = settlementService.getAssetClearance(id);
            return ResponseEntity.ok(ApiResponse.success("Asset clearance details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 5. GET TIMELINE ───────────────────────────────────────────────────────
    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            List<SettlementTimelineItem> data = settlementService.getTimeline(id);
            return ResponseEntity.ok(ApiResponse.success("Settlement timeline retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 6. SEND BACK TO HR ─────────────────────────────────────────────────────
    @PatchMapping("/{id}/send-back")
    public ResponseEntity<?> sendBack(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody SendBackRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String remarks = request != null ? request.getReason() : "Asset deduction mismatch";
            settlementService.sendBack(id, remarks, user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Settlement sent back to HR"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 7. REJECT SETTLEMENT ──────────────────────────────────────────────────
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody RejectRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String remarks = request != null ? request.getReason() : "Settlement rejected";
            settlementService.reject(id, remarks, user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Settlement rejected successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 8. APPROVE SETTLEMENT ─────────────────────────────────────────────────
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody ApproveRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            String remarks = request != null ? request.getRemarks() : "Verified and approved";
            FnfSettlement approved = settlementService.approve(id, remarks, user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Settlement approved successfully", approved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 9. PROCESS SETTLEMENT ─────────────────────────────────────────────────
    @PostMapping("/{id}/process")
    public ResponseEntity<?> process(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody ProcessRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            if (request == null || request.getPaymentMode() == null || request.getTransactionReference() == null) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("Payment mode and transaction reference are required.", "FNF_002"));
            }
            FnfSettlement processed = settlementService.process(id, request.getPaymentMode(), request.getTransactionReference(), user.getFullName());
            return ResponseEntity.ok(ApiResponse.success("Settlement processed successfully", processed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 10. GET STATUS DETAILS ────────────────────────────────────────────────
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatusDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            SettlementStatusResponse data = settlementService.getStatusDetails(id);
            return ResponseEntity.ok(ApiResponse.success("Settlement status details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 11. GET PDF METADATA ──────────────────────────────────────────────────
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> getPdfMetadata(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            SettlementPdfResponse data = settlementService.getPdfMetadata(id);
            return ResponseEntity.ok(ApiResponse.success("PDF metadata retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    // ── 12. GET REPORTS SUMMARY ───────────────────────────────────────────────
    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportsSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        SettlementReportsSummaryResponse data = settlementService.getReportsSummary();
        return ResponseEntity.ok(ApiResponse.success("Settlement reports summary retrieved successfully", data));
    }

    // ── 13. EXPORT SETTLEMENTS CSV ────────────────────────────────────────────
    @GetMapping("/export")
    public ResponseEntity<?> exportSettlements(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        SettlementExportResponse data = new SettlementExportResponse("settlements-export.csv", "/api/v1/files/settlements-export.csv");
        return ResponseEntity.ok(ApiResponse.success("Settlements export metadata retrieved successfully", data));
    }
}
