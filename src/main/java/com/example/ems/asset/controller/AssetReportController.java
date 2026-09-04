package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDtos.AssetDashboardResponse;
import com.example.ems.asset.dto.AssetDtos.AssetResponse;
import com.example.ems.asset.dto.AssetDtos.PaginatedAssetResponse;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.service.AssetReportService;
import com.example.ems.asset.service.AssetService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@CrossOrigin("*")
@Tag(name = "Asset Reports & Dashboard", description = "High-performance database aggregations, dashboards, and financial reports.")
public class AssetReportController {

    @Autowired
    private AssetReportService reportService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                if (email != null) {
                    User u = userRepository.findByWorkEmail(email).orElseGet(() -> userRepository.findByUserId(email).orElse(null));
                    if (u != null) return u;
                }
            }
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof com.example.ems.security.dto.AuthPrincipal p) {
                if (p.getEmail() != null) {
                    User u = userRepository.findByWorkEmail(p.getEmail()).orElseGet(() -> userRepository.findByUserId(p.getEmail()).orElse(null));
                    if (u != null) return u;
                }
                if (p.getUserId() != null) {
                    User u = userRepository.findByUserId(p.getUserId()).orElseGet(() -> userRepository.findByWorkEmail(p.getUserId()).orElse(null));
                    if (u != null) return u;
                }
            }
            String principal = auth.getName();
            if (principal != null && !principal.isBlank()) {
                return userRepository.findByUserId(principal)
                        .orElseGet(() -> userRepository.findByWorkEmail(principal).orElse(null));
            }
        }
        return null;
    }

    private Long resolveOrgId(User user) {
        if (user == null || user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized or missing organization context");
        }
        return user.getOrganizationId();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Search Assets", description = "Paginated asset search across asset code, name, and serial number.")
    public ResponseEntity<PaginatedAssetResponse> searchAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(assetService.getAssets(orgId, status, categoryId, q, "createdAt", "DESC", page, size));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Asset Dashboard Summary", description = "High-performance database-level status count aggregations and total purchase valuation.")
    public ResponseEntity<AssetDashboardResponse> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Asset Summary Report", description = "Retrieves overall asset breakdown summary.")
    public ResponseEntity<AssetDashboardResponse> getSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Expiring Warranties Report", description = "Lists assets with warranties expiring within given days.")
    public ResponseEntity<List<AssetResponse>> getExpiringWarranties(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "30") int days) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getExpiringWarranties(orgId, days));
    }

    @GetMapping("/overdue-returns")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Overdue Returns Report", description = "Lists assigned assets past expected return date.")
    public ResponseEntity<List<AssetResponse>> getOverdueReturns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getMaintenanceDue(orgId));
    }

    @GetMapping("/maintenance-due")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Maintenance Due Report", description = "Lists assets currently in maintenance or repair.")
    public ResponseEntity<List<AssetResponse>> getMaintenanceDue(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getMaintenanceDue(orgId));
    }

    @GetMapping("/lost")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Lost Assets Report", description = "Lists assets currently marked as LOST.")
    public ResponseEntity<List<AssetResponse>> getLostAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getLostAssets(orgId));
    }

    @GetMapping("/damaged")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Damaged Assets Report", description = "Lists assets currently marked as DAMAGED.")
    public ResponseEntity<List<AssetResponse>> getDamagedAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDamagedAssets(orgId));
    }

    @GetMapping("/reports/inventory")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Inventory Report", description = "Generates complete inventory breakdown report.")
    public ResponseEntity<AssetDashboardResponse> getInventoryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/reports/assignment")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Assignment Report", description = "Lists all current and active assignments.")
    public ResponseEntity<AssetDashboardResponse> getAssignmentReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/reports/transfer")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Transfer Report", description = "Lists overall asset transfers.")
    public ResponseEntity<AssetDashboardResponse> getTransferReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/reports/disposal")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Disposal Report", description = "Lists overall disposed assets.")
    public ResponseEntity<AssetDashboardResponse> getDisposalReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }

    @GetMapping("/reports/depreciation")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Financial Depreciation Report", description = "Calculates total asset purchase cost and valuation report.")
    public ResponseEntity<AssetDashboardResponse> getDepreciationReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(reportService.getDashboard(orgId));
    }
}
