package com.example.ems.support.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.PermissionCheckService;
import com.example.ems.support.dto.SupportEscalationRulesDto;
import com.example.ems.support.dto.SupportSlaConfigDto;
import com.example.ems.support.service.SupportSlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/support/sla", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Support SLA & Escalations", description = "Organization-level SLA and Escalation Configuration APIs")
public class SupportSlaController {

    @Autowired
    private SupportSlaService slaService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 14. SLA Configuration (GET /api/v1/support/sla)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get SLA Configuration", description = "Retrieves the organization-level SLA rules")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SLA configuration retrieved",
                    content = @Content(schema = @Schema(implementation = SupportSlaConfigDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getSlaConfig() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.SUPPORT_TICKET_SLA_MANAGE,
                PermissionRegistry.SUPPORT_TICKET_VIEW
        );
        SupportSlaConfigDto response = slaService.getSlaConfig();
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 15. Update SLA Configuration (PUT /api/v1/support/sla)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update SLA Configuration", description = "Updates organization-level SLA rules")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SLA configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = SupportSlaConfigDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Missing SUPPORT_TICKET_SLA_MANAGE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<?> updateSlaConfig(@Valid @RequestBody SupportSlaConfigDto req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_SLA_MANAGE);
        SupportSlaConfigDto response = slaService.updateSlaConfig(req);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 16. Escalation Rules (GET /api/v1/support/sla/escalations)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Escalation Rules", description = "Retrieves tiered escalation rules for the organization")
    @GetMapping("/escalations")
    public ResponseEntity<?> getEscalationRules() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.SUPPORT_TICKET_SLA_MANAGE,
                PermissionRegistry.SUPPORT_TICKET_VIEW
        );
        SupportEscalationRulesDto response = slaService.getEscalationRules();
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 17. Update Escalation Rules (PUT /api/v1/support/sla/escalations)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update Escalation Rules", description = "Updates tiered escalation rules for the organization")
    @PutMapping("/escalations")
    public ResponseEntity<?> updateEscalationRules(@Valid @RequestBody SupportEscalationRulesDto req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_SLA_MANAGE);
        SupportEscalationRulesDto response = slaService.updateEscalationRules(req);
        return ResponseEntity.ok(response);
    }
}
