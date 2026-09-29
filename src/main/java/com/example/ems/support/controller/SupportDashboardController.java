package com.example.ems.support.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.PermissionCheckService;
import com.example.ems.support.dto.SupportDashboardSummaryResponse;
import com.example.ems.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/support/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Support Dashboard", description = "Tenant-level Support Dashboard Metrics API")
public class SupportDashboardController {

    @Autowired
    private SupportTicketService ticketService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 22. Dashboard Summary API (GET /api/v1/support/dashboard)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Support Dashboard Summary", description = "Returns aggregated tenant-scoped support metrics")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Support dashboard summary retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Missing SUPPORT_TICKET_VIEW or SUPPORT_TICKET_REPORT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<SupportDashboardSummaryResponse>> getDashboardSummary() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.SUPPORT_TICKET_VIEW,
                PermissionRegistry.SUPPORT_TICKET_REPORT
        );
        SupportDashboardSummaryResponse response = ticketService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Support dashboard retrieved successfully", response));
    }
}
