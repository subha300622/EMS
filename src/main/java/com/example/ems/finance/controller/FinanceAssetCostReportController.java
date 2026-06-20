package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceAssetCostReportService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/asset-cost-report")
@CrossOrigin("*")
@Tag(name = "Reports & Analytics")
public class FinanceAssetCostReportController {

    @Autowired
    private FinanceAssetCostReportService reportService;

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

        AssetCostDashboardResponse data = reportService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Asset cost dashboard retrieved successfully", data));
    }

    // ── 2. GET BREAKDOWN (PAGINATED) ──────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getBreakdown(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> data = reportService.getBreakdown(pageable);
        return ResponseEntity.ok(ApiResponse.success("Asset cost breakdown retrieved successfully", data));
    }

    // ── 3. GET CATEGORY DETAILS ───────────────────────────────────────────────
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getCategoryDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("categoryId") Long categoryId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            CategoryCostDetailsResponse data = reportService.getCategoryDetails(categoryId);
            return ResponseEntity.ok(ApiResponse.success("Category cost details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "CAT_404"));
        }
    }

    // ── 4. GET CATEGORY ASSETS ────────────────────────────────────────────────
    @GetMapping("/categories/{categoryId}/assets")
    public ResponseEntity<?> getCategoryAssets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("categoryId") Long categoryId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            CategoryAssetsResponse data = reportService.getCategoryAssets(categoryId);
            return ResponseEntity.ok(ApiResponse.success("Category assets retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "CAT_404"));
        }
    }

    // ── 5. GET ASSET FINANCIAL DETAILS ────────────────────────────────────────
    @GetMapping("/assets/{assetId}")
    public ResponseEntity<?> getAssetFinancialDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("assetId") Long assetId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        try {
            AssetFinancialDetailsResponse data = reportService.getAssetFinancialDetails(assetId);
            return ResponseEntity.ok(ApiResponse.success("Asset financial details retrieved successfully", data));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "AST_404"));
        }
    }

    // ── 6. GET DEPRECIATION REPORT ────────────────────────────────────────────
    @GetMapping("/depreciation")
    public ResponseEntity<?> getDepreciationReport(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        DepreciationReportResponse data = reportService.getDepreciationReport();
        return ResponseEntity.ok(ApiResponse.success("Depreciation report retrieved successfully", data));
    }

    // ── 7. GET MAINTENANCE COST REPORT ────────────────────────────────────────
    @GetMapping("/maintenance-cost")
    public ResponseEntity<?> getMaintenanceCostReport(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        MaintenanceCostReportResponse data = reportService.getMaintenanceCostReport();
        return ResponseEntity.ok(ApiResponse.success("Maintenance cost report retrieved successfully", data));
    }

    // ── 8. GET REPLACEMENT DUE ASSETS ────────────────────────────────────────
    @GetMapping("/replacement-due")
    public ResponseEntity<?> getReplacementDueAssets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        ReplacementDueAssetsResponse data = reportService.getReplacementDueAssets();
        return ResponseEntity.ok(ApiResponse.success("Replacement due assets retrieved successfully", data));
    }

    // ── 9. EXPORT PDF ─────────────────────────────────────────────────────────
    @GetMapping("/export/pdf")
    public ResponseEntity<?> exportPdf(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        ExportReportResponse data = reportService.exportPdf();
        return ResponseEntity.ok(ApiResponse.success("PDF export generated successfully", data));
    }

    // ── 10. EXPORT CSV ────────────────────────────────────────────────────────
    @GetMapping("/export/csv")
    public ResponseEntity<?> exportCsv(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkAccess(user)) return forbiddenResponse();

        ExportReportResponse data = reportService.exportCsv();
        return ResponseEntity.ok(ApiResponse.success("CSV export generated successfully", data));
    }
}
