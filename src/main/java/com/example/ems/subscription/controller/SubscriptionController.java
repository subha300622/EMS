package com.example.ems.subscription.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.security.service.JwtService;
import com.example.ems.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform-admin/subscriptions")
@CrossOrigin("*")
@Tag(name = "Platform Administration Subscriptions", description = "Decoupled SaaS Subscription Lifecycles")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.ems.subscription.service.SubscriptionAnalyticsService analyticsService;

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
                    .body(ErrorResponse.error("Access Denied: Requires platform admin permission.", "AUTH_002"));
        }
        return null;
    }

    @Operation(summary = "Create Subscription")
    @PostMapping("/organization/{organizationId}")
    public ResponseEntity<?> createSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long organizationId,
            @RequestBody @Valid CreateSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            SubscriptionResponse response = subscriptionService.createSubscription(organizationId, request, user.getWorkEmail());
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("subscription", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId());
            links.put("invoice", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId() + "/invoices");
            links.put("usage", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId() + "/usage");
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Subscription created successfully.", response, links));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUB_001"));
        }
    }

    @Operation(summary = "Create Subscription (Body-based)")
    @PostMapping
    public ResponseEntity<?> createSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid CreateSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        Long orgId = request.organizationId();
        if (orgId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Organization ID is required", "SUB_003"));
        }

        User user = resolveUser(authHeader);
        try {
            SubscriptionResponse response = subscriptionService.createSubscription(orgId, request, user.getWorkEmail());
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("subscription", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId());
            links.put("invoice", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId() + "/invoices");
            links.put("usage", "/api/v1/platform-admin/subscriptions/" + response.subscriptionId() + "/usage");
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Subscription created successfully. Invoice has been generated and is awaiting payment.", response, links));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUB_001"));
        }
    }

    @Operation(summary = "Get Active Subscription")
    @GetMapping("/organization/{organizationId}/active")
    public ResponseEntity<?> getActiveSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long organizationId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            SubscriptionResponse response = subscriptionService.getActiveSubscription(organizationId);
            return ResponseEntity.ok(ApiResponse.success("Active subscription retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Subscription Details")
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<?> getSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            SubscriptionResponse response = subscriptionService.getSubscription(subscriptionId);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", "/api/v1/platform-admin/subscriptions/" + subscriptionId);
            links.put("organization", "/api/v1/platform-admin/organizations/" + response.organizationId());
            links.put("invoices", "/api/v1/platform-admin/subscriptions/" + subscriptionId + "/invoices");
            links.put("usage", "/api/v1/platform-admin/subscriptions/" + subscriptionId + "/usage");
            return ResponseEntity.ok(ApiResponse.success("Subscription details retrieved", response, links));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Subscription History")
    @GetMapping("/organization/{organizationId}/history")
    public ResponseEntity<?> getSubscriptionHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long organizationId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        List<SubscriptionResponse> history = subscriptionService.getSubscriptionHistory(organizationId);
        return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved", history));
    }

    @Operation(summary = "Upgrade Subscription Plan")
    @PutMapping("/{subscriptionId}/upgrade")
    public ResponseEntity<?> upgradeSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid UpgradeSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            SubscriptionResponse response = subscriptionService.upgradeSubscription(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription upgraded successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUB_001"));
        }
    }

    @Operation(summary = "Downgrade Subscription Plan")
    @PutMapping("/{subscriptionId}/downgrade")
    public ResponseEntity<?> downgradeSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid DowngradeSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            SubscriptionResponse response = subscriptionService.downgradeSubscription(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription downgraded successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUB_001"));
        }
    }

    @Operation(summary = "Renew Subscription")
    @PutMapping("/{subscriptionId}/renew")
    public ResponseEntity<?> renewSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid RenewSubscriptionRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            SubscriptionResponse response = subscriptionService.renewSubscription(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription renewed successfully.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUB_001"));
        }
    }

    @Operation(summary = "Cancel Subscription")
    @PutMapping("/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody Map<String, String> body) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        String reason = body.getOrDefault("reason", "No reason provided");
        try {
            subscriptionService.cancelSubscription(subscriptionId, reason, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Suspend Subscription")
    @PutMapping("/{subscriptionId}/suspend")
    public ResponseEntity<?> suspendSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody Map<String, String> body) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        String reason = body.getOrDefault("reason", "No reason provided");
        try {
            subscriptionService.suspendSubscription(subscriptionId, reason, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription suspended successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Resume Subscription")
    @PutMapping("/{subscriptionId}/resume")
    public ResponseEntity<?> resumeSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            subscriptionService.resumeSubscription(subscriptionId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription resumed successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Update Auto Renew Status")
    @PutMapping("/{subscriptionId}/auto-renew")
    public ResponseEntity<?> updateAutoRenew(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid AutoRenewRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            subscriptionService.updateAutoRenew(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Auto renew status updated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Update Enabled Modules")
    @PutMapping("/{subscriptionId}/modules")
    public ResponseEntity<?> updateModules(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid ModulesRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            subscriptionService.updateModules(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription modules updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Update Quotas and Limits")
    @PutMapping("/{subscriptionId}/limits")
    public ResponseEntity<?> updateLimits(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId,
            @RequestBody @Valid LimitsRequest request) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.subscription");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            subscriptionService.updateLimits(subscriptionId, request, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription limits updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Current Resource Usage")
    @GetMapping("/{subscriptionId}/usage")
    public ResponseEntity<?> getUsage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            SubscriptionUsageResponse response = subscriptionService.getUsage(subscriptionId);
            return ResponseEntity.ok(ApiResponse.success("Subscription usage retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Subscription History Log")
    @GetMapping("/{subscriptionId}/history")
    public ResponseEntity<?> getHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            List<SubscriptionHistoryResponse> history = subscriptionService.getHistory(subscriptionId);
            return ResponseEntity.ok(ApiResponse.success("Subscription audit log history retrieved", history));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Invoice History")
    @GetMapping("/{subscriptionId}/invoices")
    public ResponseEntity<?> getInvoices(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        List<InvoiceResponse> invoices = subscriptionService.getInvoices(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success("Subscription invoices retrieved", invoices));
    }

    @Operation(summary = "Get Invoice Details")
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<?> getInvoiceDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long invoiceId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            InvoiceResponse response = subscriptionService.getInvoiceDetails(invoiceId);
            return ResponseEntity.ok(ApiResponse.success("Invoice details retrieved", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_003"));
        }
    }

    @Operation(summary = "Download Invoice PDF")
    @GetMapping("/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long invoiceId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        try {
            byte[] fileBytes = subscriptionService.downloadInvoice(invoiceId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_003"));
        }
    }

    @Operation(summary = "Delete Subscription")
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<?> deleteSubscription(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long subscriptionId) {

        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.delete");
        if (accessError != null) return accessError;

        User user = resolveUser(authHeader);
        try {
            subscriptionService.deleteSubscription(subscriptionId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Subscription deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "SUB_002"));
        }
    }

    @Operation(summary = "Get Subscription Overview Dashboard")
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Subscription overview dashboard retrieved", analyticsService.getOverview()));
    }

    @Operation(summary = "Get MRR Analytics")
    @GetMapping("/metrics/mrr")
    public ResponseEntity<?> getMRR(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("MRR metric retrieved", analyticsService.getMRR()));
    }

    @Operation(summary = "Get Active Subscriptions Metric")
    @GetMapping("/metrics/active")
    public ResponseEntity<?> getActiveSubscriptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Active subscriptions metric retrieved", analyticsService.getActiveSubscriptions()));
    }

    @Operation(summary = "Get Revenue Collected Metric")
    @GetMapping("/metrics/revenue")
    public ResponseEntity<?> getRevenueCollected(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Revenue collected metric retrieved", analyticsService.getRevenueCollected()));
    }

    @Operation(summary = "Get Invoice Health Metrics")
    @GetMapping("/metrics/invoices")
    public ResponseEntity<?> getInvoiceHealth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Invoice health metric retrieved", analyticsService.getOverdueInvoices()));
    }

    @Operation(summary = "Get Plan Distribution")
    @GetMapping("/metrics/plan-distribution")
    public ResponseEntity<?> getPlanDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Plan distribution retrieved", analyticsService.getPlanDistribution()));
    }

    @Operation(summary = "Get Renewal Forecast")
    @GetMapping("/metrics/renewals")
    public ResponseEntity<?> getRenewals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "days", defaultValue = "30") int days) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Upcoming renewals forecast retrieved", analyticsService.getUpcomingRenewals(days)));
    }

    @Operation(summary = "Get Churn & Retention Analytics")
    @GetMapping("/metrics/churn")
    public ResponseEntity<?> getChurn(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        ResponseEntity<?> accessError = validateAccess(authHeader, "organization.read");
        if (accessError != null) return accessError;

        return ResponseEntity.ok(ApiResponse.success("Churn metric retrieved", analyticsService.getChurnRate()));
    }
}
