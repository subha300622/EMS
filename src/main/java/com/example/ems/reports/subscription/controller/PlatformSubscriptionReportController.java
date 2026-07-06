package com.example.ems.reports.subscription.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.subscription.facade.SubscriptionReportFacade;
import com.example.ems.reports.subscription.dto.*;
import com.example.ems.reports.subscription.validator.SubscriptionReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/platform/reports/subscriptions", "/api/v1/platform/reports/subscriptions"})
@CrossOrigin("*")
@Tag(name = "Platform Subscription Reports", description = "Detailed subscription reports and export tools for platform admins")
public class PlatformSubscriptionReportController {

    @Autowired
    private SubscriptionReportFacade reportFacade;

    @Autowired
    private SubscriptionReportValidator validator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogService auditLogService;

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

    private ResponseEntity<?> validateAccess(String authHeader, String requiredPermission) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(user.getWorkEmail(), requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires permission " + requiredPermission, "AUTH_002"));
        }
        return null;
    }

    private void logAuditEvent(String authHeader, String action, String details) {
        User user = resolveUser(authHeader);
        if (user != null) {
            auditLogService.logAction(
                    user.getEmployeeId(), 
                    user.getWorkEmail(), 
                    action, 
                    "Subscription Report", 
                    "REPORT", 
                    "127.0.0.1", 
                    details
            );
        }
    }

    @Operation(summary = "Get paginated list of organization subscriptions")
    @GetMapping
    public ResponseEntity<?> getSubscriptionList(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "organizationId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionReportFilterRequest filter = new SubscriptionReportFilterRequest();
        filter.setSearch(search);
        filter.setStatus(status);
        filter.setPlan(plan);
        filter.setBillingCycle(billingCycle);
        filter.setFromDate(from);
        filter.setToDate(to);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setDirection(direction);

        validator.validateFilter(filter);

        Page<OrgSubscriptionListItem> data = reportFacade.getSubscriptionList(filter);
        logAuditEvent(authHeader, "VIEW", "Subscription reports viewed");
        return ResponseEntity.ok(ApiResponse.success("Subscription list loaded successfully", data));
    }

    @Operation(summary = "Get list of expiring subscriptions")
    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringSubscriptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        Pageable pageable = PageRequest.of(page, size);
        Page<ExpiringSubscriptionEntry> data = reportFacade.getExpiringSubscriptions(days, pageable);
        logAuditEvent(authHeader, "VIEW", "Expiring subscription list viewed (days threshold: " + days + ")");
        return ResponseEntity.ok(ApiResponse.success("Expiring subscriptions list loaded successfully", data));
    }

    @Operation(summary = "Get list of active trial subscriptions")
    @GetMapping("/trials")
    public ResponseEntity<?> getTrialOrganizations(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        Pageable pageable = PageRequest.of(page, size);
        Page<TrialOrganizationEntry> data = reportFacade.getTrialOrganizations(pageable);
        logAuditEvent(authHeader, "VIEW", "Active trial subscriptions list viewed");
        return ResponseEntity.ok(ApiResponse.success("Trial organizations list loaded successfully", data));
    }

    @Operation(summary = "Get detailed subscription report for single organization")
    @GetMapping("/{organizationId}")
    public ResponseEntity<?> getSubscriptionDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long organizationId) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_SUBSCRIPTION_VIEW);
        if (accessCheck != null) return accessCheck;

        SubscriptionDetailResponse data = reportFacade.getSubscriptionDetail(organizationId);
        logAuditEvent(authHeader, "VIEW", "Subscription detail report viewed for organization ID: " + organizationId);
        return ResponseEntity.ok(ApiResponse.success("Subscription details loaded successfully", data));
    }

    @Operation(summary = "Export subscription report")
    @GetMapping("/export")
    public ResponseEntity<?> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REPORTS_SUBSCRIPTION_EXPORT);
        if (accessCheck != null) return accessCheck;

        SubscriptionExportRequest request = new SubscriptionExportRequest();
        request.setFormat(format);
        request.setSearch(search);
        request.setStatus(status);
        request.setPlan(plan);
        request.setBillingCycle(billingCycle);
        request.setFromDate(from);
        request.setToDate(to);

        validator.validateExport(request);

        byte[] fileBytes = reportFacade.exportReport(request);
        String ext = format.equalsIgnoreCase("CSV") ? ".csv" : (format.equalsIgnoreCase("EXCEL") ? ".xlsx" : ".pdf");
        String filename = "subscription-report" + ext;

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (format.equalsIgnoreCase("CSV")) {
            mediaType = MediaType.parseMediaType("text/csv");
        } else if (format.equalsIgnoreCase("PDF")) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);

        logAuditEvent(authHeader, "EXPORT", "Subscription report exported in " + format.toUpperCase() + " format");
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}
