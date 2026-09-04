package com.example.ems.reports.revenue.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.reports.revenue.dto.*;
import com.example.ems.reports.revenue.facade.RevenueReportFacade;
import com.example.ems.reports.revenue.validator.RevenueReportValidator;
import com.example.ems.security.service.JwtService;
import com.example.ems.audit.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform/revenue/reports")
@CrossOrigin("*")
@Tag(name = "Platform Revenue Reports", description = "Detailed financial reporting datasets for platform admins")
public class PlatformRevenueReportController {

    @Autowired
    private RevenueReportFacade reportFacade;

    @Autowired
    private RevenueReportValidator reportValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuditLogService auditLogService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    private String getCurrentClientIp() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return com.example.ems.common.util.ClientIpResolver.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }

    private void logStructuredAuditFilter(String authHeader, String eventName, String reportType, RevenueFilterRequest filters) {
        User user = resolveUser(authHeader);
        if (user != null) {
            String clientIp = getCurrentClientIp();
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("event", eventName);
                payload.put("userId", user.getWorkEmail());
                payload.put("reportType", reportType);

                Map<String, Object> filterMap = new HashMap<>();
                if (filters != null) {
                    filterMap.put("from", filters.getFrom());
                    filterMap.put("to", filters.getTo());
                    filterMap.put("organizationId", filters.getOrganizationId());
                    filterMap.put("subscriptionPlan", filters.getSubscriptionPlan());
                    filterMap.put("currency", filters.getCurrency());
                    filterMap.put("gateway", filters.getGateway());
                    filterMap.put("paymentMethod", filters.getPaymentMethod());
                    filterMap.put("invoiceStatus", filters.getInvoiceStatus());
                    filterMap.put("billingCycle", filters.getBillingCycle());
                    filterMap.put("country", filters.getCountry());
                }
                payload.put("filters", filterMap);

                String detailsJson = OBJECT_MAPPER.writeValueAsString(payload);

                auditLogService.logAction(
                        user.getEmployeeId(), 
                        user.getWorkEmail(), 
                        eventName, 
                        "Revenue Reports", 
                        "REVENUE", 
                        clientIp, 
                        detailsJson
                );
            } catch (Exception e) {
                // fallback to regular log
                auditLogService.logAction(
                        user.getEmployeeId(), 
                        user.getWorkEmail(), 
                        eventName, 
                        "Revenue Reports", 
                        "REVENUE", 
                        clientIp, 
                        "Failed to map structured payload, fallback logged. Filters: " + filters
                );
            }
        }
    }

    private void logAuditEvent(String authHeader, String action, String details) {
        User user = resolveUser(authHeader);
        if (user != null) {
            auditLogService.logAction(
                    user.getEmployeeId(), 
                    user.getWorkEmail(), 
                    action, 
                    "Revenue Reports", 
                    "REVENUE", 
                    getCurrentClientIp(), 
                    details
            );
        }
    }

    @Operation(summary = "Get detailed payments transaction report")
    @GetMapping("/payments")
    public ResponseEntity<?> getPaymentsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String subscriptionPlan,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String gateway,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean autoRenewal,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_PAYMENTS_VIEW);
        if (accessCheck != null) return accessCheck;

        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setFrom(from);
        filters.setTo(to);
        filters.setOrganizationId(organizationId);
        filters.setSubscriptionPlan(subscriptionPlan);
        filters.setPaymentStatus(paymentStatus);
        filters.setCurrency(currency);
        filters.setGateway(gateway);
        filters.setPaymentMethod(paymentMethod);
        filters.setBillingCycle(billingCycle);
        filters.setCountry(country);
        filters.setIndustry(industry);
        filters.setAutoRenewal(autoRenewal);
        filters.setMinAmount(minAmount);
        filters.setMaxAmount(maxAmount);
        filters.setPage(page);
        filters.setSize(size);
        filters.setSortBy(sortBy);
        filters.setDirection(direction);

        reportValidator.validateFilter(filters);
        Page<RevenuePaymentResponse> data = reportFacade.getPaymentsReport(filters);

        logStructuredAuditFilter(authHeader, "REVENUE_REPORT_FILTERED", "PAYMENTS", filters);
        logAuditEvent(authHeader, "Payments Report Viewed", "Payments transaction list viewed");

        return ResponseEntity.ok(ApiResponse.success("Payments transaction report loaded successfully", data));
    }

    @Operation(summary = "Get detailed invoices report")
    @GetMapping("/invoices")
    public ResponseEntity<?> getInvoicesReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String subscriptionPlan,
            @RequestParam(required = false) String invoiceStatus,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String billingCycle,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean autoRenewal,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_INVOICES_VIEW);
        if (accessCheck != null) return accessCheck;

        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setFrom(from);
        filters.setTo(to);
        filters.setOrganizationId(organizationId);
        filters.setSubscriptionPlan(subscriptionPlan);
        filters.setInvoiceStatus(invoiceStatus);
        filters.setCurrency(currency);
        filters.setBillingCycle(billingCycle);
        filters.setCountry(country);
        filters.setAutoRenewal(autoRenewal);
        filters.setMinAmount(minAmount);
        filters.setMaxAmount(maxAmount);
        filters.setPage(page);
        filters.setSize(size);
        filters.setSortBy(sortBy);
        filters.setDirection(direction);

        reportValidator.validateFilter(filters);
        Page<RevenueInvoiceResponse> data = reportFacade.getInvoicesReport(filters);

        logStructuredAuditFilter(authHeader, "REVENUE_REPORT_FILTERED", "INVOICES", filters);
        logAuditEvent(authHeader, "Invoices Report Viewed", "Invoices report viewed");

        return ResponseEntity.ok(ApiResponse.success("Invoices report loaded successfully", data));
    }

    @Operation(summary = "Get detailed refunds activity report")
    @GetMapping("/refunds")
    public ResponseEntity<?> getRefundsReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String subscriptionPlan,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String gateway,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_REFUNDS_VIEW);
        if (accessCheck != null) return accessCheck;

        RevenueFilterRequest filters = new RevenueFilterRequest();
        filters.setFrom(from);
        filters.setTo(to);
        filters.setOrganizationId(organizationId);
        filters.setSubscriptionPlan(subscriptionPlan);
        filters.setCurrency(currency);
        filters.setGateway(gateway);
        filters.setCountry(country);
        filters.setMinAmount(minAmount);
        filters.setMaxAmount(maxAmount);
        filters.setPage(page);
        filters.setSize(size);
        filters.setSortBy(sortBy);
        filters.setDirection(direction);

        reportValidator.validateFilter(filters);
        Page<RevenueRefundResponse> data = reportFacade.getRefundsReport(filters);

        logStructuredAuditFilter(authHeader, "REVENUE_REPORT_FILTERED", "REFUNDS", filters);
        logAuditEvent(authHeader, "Refund Report Viewed", "Refunds activity report viewed");

        return ResponseEntity.ok(ApiResponse.success("Refund activity report loaded successfully", data));
    }

    @Operation(summary = "Get plan revenue distribution report")
    @GetMapping("/plans")
    public ResponseEntity<?> getPlansReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_PLANS_VIEW);
        if (accessCheck != null) return accessCheck;

        List<RevenuePlanDistributionResponse> data = reportFacade.getPlansReport();
        logAuditEvent(authHeader, "Plans Report Viewed", "Plan revenue distribution viewed");
        return ResponseEntity.ok(ApiResponse.success("Plan revenue report loaded successfully", data));
    }

    @Operation(summary = "Export financial revenue reports")
    @PostMapping("/export")
    public ResponseEntity<?> exportReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RevenueExportRequest request) {
        ResponseEntity<?> accessCheck = validateAccess(authHeader, PermissionRegistry.PLATFORM_REVENUE_EXPORT);
        if (accessCheck != null) return accessCheck;

        try {
            reportValidator.validateExport(request);

            byte[] fileBytes = reportFacade.exportReport(request);
            String format = request.getFormat().toUpperCase();
            String ext = format.equals("CSV") ? ".csv" : (format.equals("EXCEL") ? ".xlsx" : ".pdf");
            String filename = "revenue-" + request.getType().toLowerCase() + "-report" + ext;

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (format.equals("CSV")) {
                mediaType = MediaType.parseMediaType("text/csv");
            } else if (format.equals("PDF")) {
                mediaType = MediaType.APPLICATION_PDF;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", filename);

            logAuditEvent(authHeader, "Revenue Exported", "Revenue report exported: type=" + request.getType() + ", format=" + format);
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logAuditEvent(authHeader, "REVENUE_EXPORT_FAILED", "Failed to export revenue report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Export failed: " + e.getMessage(), "REV_001"));
        }
    }
}
