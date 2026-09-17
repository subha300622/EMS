package com.example.ems.onboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.entity.FinanceOnboardingHistory;
import com.example.ems.finance.service.EmployeeFinanceOnboardingService;
import com.example.ems.onboarding.dto.ApprovalActionRequest;
import com.example.ems.onboarding.dto.OnboardingResponse;
import com.example.ems.onboarding.dto.OnboardingTimelineEventDto;
import com.example.ems.onboarding.dto.approval.OnboardingApprovalActionRequest;
import com.example.ems.onboarding.dto.approval.OnboardingApprovalListResponse;
import com.example.ems.onboarding.service.OnboardingApprovalService;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@Tag(name = "Centralized Approvals Engine")
public class ApprovalController {

    @Autowired
    private OnboardingApprovalService onboardingApprovalService;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private EmployeeFinanceOnboardingService financeOnboardingService;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping(value = "/api/v1/onboarding/{onboardingId}/approvals", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Onboarding Approvals History & Current Status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approvals history retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnboardingApprovalListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getOnboardingApprovals(
            @PathVariable Long onboardingId) {
        OnboardingApprovalListResponse response = onboardingApprovalService.getApprovals(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Approvals history retrieved successfully", response));
    }

    @PostMapping(value = "/api/v1/onboarding/{onboardingId}/approvals", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process Onboarding Approval Action (Approve / Reject)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval action processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnboardingApprovalListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid approval action request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> processApproval(
            @PathVariable Long onboardingId,
            @Valid @RequestBody OnboardingApprovalActionRequest request) {
        OnboardingApprovalListResponse response = onboardingApprovalService.processApprovalAction(onboardingId, request);
        return ResponseEntity.ok(ApiResponse.success("Approval action processed successfully", response));
    }

    @PostMapping(value = "/api/v1/approvals", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Centralized approval action handler for onboarding or finance")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval action processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OnboardingResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid action or parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Target entity not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> handleApproval(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid ApprovalActionRequest body) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String entityType = body != null ? body.entityType() : null;
        Long entityId = body != null ? body.entityId() : null;
        String action = body != null ? body.action() : null;
        String notes = body != null ? body.getEffectiveNotes() : "Action processed by approvals engine";

        if (entityType == null || entityId == null || action == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("entityType, entityId, and action are required", "VAL_001"));
        }

        try {
            if ("ONBOARDING".equalsIgnoreCase(entityType)) {
                if ("APPROVE".equalsIgnoreCase(action)) {
                    var resOpt = onboardingService.approveOnboarding(entityId);
                    if (resOpt.isPresent()) {
                        return ResponseEntity.ok(ApiResponse.success("Onboarding approved successfully", (Object) resOpt.get()));
                    }
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Onboarding not found", "ONB_002"));
                } else if ("COMPLETE".equalsIgnoreCase(action)) {
                    var resOpt = onboardingService.completeOnboarding(entityId);
                    if (resOpt.isPresent()) {
                        return ResponseEntity.ok(ApiResponse.success("Onboarding completed successfully", (Object) resOpt.get()));
                    }
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Onboarding not found", "ONB_002"));
                } else {
                    return ResponseEntity.badRequest()
                            .body(ErrorResponse.error("Unsupported action for ONBOARDING entity", "VAL_002"));
                }
            } else if ("FINANCE".equalsIgnoreCase(entityType)) {
                if ("APPROVE".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.approve(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding approved successfully", res));
                } else if ("REJECT".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.reject(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding rejected successfully", res));
                } else if ("SEND_BACK".equalsIgnoreCase(action)) {
                    Object res = financeOnboardingService.sendBack(entityId, currentUser.getWorkEmail(), notes);
                    return ResponseEntity.ok(ApiResponse.success("Finance onboarding sent back for correction", res));
                } else {
                    return ResponseEntity.badRequest()
                            .body(ErrorResponse.error("Unsupported action for FINANCE entity", "VAL_002"));
                }
            } else {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.error("Unsupported entityType: " + entityType, "VAL_002"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "ONB_ERR"));
        }
    }

    @GetMapping(value = "/api/v1/approvals/onboarding/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get structured onboarding approval timeline history")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Onboarding approvals history timeline retrieved",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OnboardingTimelineEventDto.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<?> getOnboardingHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        List<OnboardingTimelineEventDto> timeline = onboardingService.getStructuredOnboardingTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Onboarding approvals history timeline retrieved", timeline));
    }

    @GetMapping(value = "/api/v1/approvals/finance/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get finance approvals history logs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Finance approvals history logs retrieved",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FinanceOnboardingHistory.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<?> getFinanceHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }
        List<FinanceOnboardingHistory> history = financeOnboardingService.getHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Finance approvals history logs retrieved", history));
    }
}

