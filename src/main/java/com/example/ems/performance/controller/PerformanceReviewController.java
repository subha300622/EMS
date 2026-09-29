package com.example.ems.performance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.performance.dto.*;
import com.example.ems.performance.entity.PerformanceCalculationRun;
import com.example.ems.performance.entity.PerformanceReviewAudit;
import com.example.ems.performance.service.PerformanceReviewService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/performance-reviews", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Enterprise Performance Reviews", description = "Enterprise Performance Review Lifecycle, Calculation, Gated Publishing, and Audit APIs")
public class PerformanceReviewController {

    @Autowired
    private PerformanceReviewService reviewService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

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

    private ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        String code = e.getErrorCode();
        if (code == null || "CONFLICT".equals(code)) {
            code = "PERFORMANCE_LOCKED";
        }
        String msg = e.getMessage();
        if (msg != null && msg.startsWith(code + ": ")) {
            msg = msg.substring((code + ": ").length());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(msg, code));
    }

    @Operation(summary = "Initiate Review Record", description = "Initiates an enterprise performance review record for an employee under a cycle")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Performance review initiated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review already exists or locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> initiateReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long cycleId,
            @RequestParam Long employeeId,
            @RequestParam(required = false) Long reviewerId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.initiateReview(user, cycleId, employeeId, reviewerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Performance review initiated successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Submit Self Review", description = "Employee submits self-evaluation rating and feedback")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Self review submitted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked or not in eligible state",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{reviewId}/self-review", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitSelfReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestBody(required = false) EnterpriseSelfReviewRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (request == null) request = new EnterpriseSelfReviewRequest();
        if (request.getIdempotencyKey() == null && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.submitSelfReview(user, reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Self review submitted successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Submit Manager Review", description = "Manager submits evaluation score and commentary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Manager review submitted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden: only designated reviewer/manager or HR can review",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{reviewId}/manager-review", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> submitManagerReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestBody(required = false) EnterpriseManagerReviewRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (request == null) request = new EnterpriseManagerReviewRequest();
        if (request.getIdempotencyKey() == null && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.submitManagerReview(user, reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Manager review submitted successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Calculate Review Score", description = "Computes Level 1 KPI score and Level 2 component score with snapshot and calculation run")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Performance review calculated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or calculation validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{reviewId}/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> calculateReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestBody(required = false) PerformanceCalculationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (request == null) request = new PerformanceCalculationRequest();
        if (request.getIdempotencyKey() == null && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.calculateReview(user, reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Performance review calculated successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Recalculate Review Score", description = "Recalculates review score with new inputs, incrementing calculation version")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Performance review recalculated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or calculation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked or approved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{reviewId}/recalculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> recalculateReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestBody(required = false) PerformanceCalculationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (request == null) request = new PerformanceCalculationRequest();
        if (request.getIdempotencyKey() == null && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.recalculateReview(user, reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Performance review recalculated successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Submit Review for Approval", description = "Transitions review to APPROVAL_PENDING and initiates delegated approval engine")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review submitted for approval successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or submission rule violation",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = {"/{reviewId}/submit", "/{reviewId}/submit-for-approval"})
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> submitForApproval(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestParam(required = false) String idempotencyKey) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        String effectiveKey = idempotencyKey != null ? idempotencyKey : idempotencyKeyHeader;

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.submitForApproval(user, reviewId, effectiveKey);
            return ResponseEntity.ok(ApiResponse.success("Review submitted for approval successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Reopen Rejected Review", description = "Reopens rejected review back to MANAGER_REVIEW_SUBMITTED for correction")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review reopened for correction successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request: review not in REJECTED status",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{reviewId}/reopen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> reopenReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestBody(required = false) ReopenReviewRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (request == null) request = new ReopenReviewRequest();
        if (request.getIdempotencyKey() == null && idempotencyKeyHeader != null) {
            request.setIdempotencyKey(idempotencyKeyHeader);
        }

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.reopenReview(user, reviewId, request);
            return ResponseEntity.ok(ApiResponse.success("Review reopened for correction successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Publish Review", description = "Gated publication verifying approval completion, snapshot integrity, and calculation version")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Performance review published successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request: approvals not completed or integrity check failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked or already published",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{reviewId}/publish")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> publishReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestParam(required = false) String idempotencyKey) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        String effectiveKey = idempotencyKey != null ? idempotencyKey : idempotencyKeyHeader;

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.publishReview(user, reviewId, effectiveKey);
            return ResponseEntity.ok(ApiResponse.success("Performance review published successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Lock Review", description = "Terminal lock of published review, making it strictly immutable")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Performance review locked successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request: review not published",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review already locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{reviewId}/lock")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> lockReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long reviewId,
            @RequestParam(required = false) String idempotencyKey) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        String effectiveKey = idempotencyKey != null ? idempotencyKey : idempotencyKeyHeader;

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.lockReview(user, reviewId, effectiveKey);
            return ResponseEntity.ok(ApiResponse.success("Performance review locked successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Cancel Review", description = "Cancels a performance review in draft or pending state")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Performance review cancelled successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request: review cannot be cancelled in current state",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: review is locked",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{reviewId}/cancel")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> cancelReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId,
            @RequestParam(required = false) String reason) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.cancelReview(user, reviewId, reason);
            return ResponseEntity.ok(ApiResponse.success("Performance review cancelled successfully", resp));
        } catch (ConflictException e) {
            return handleConflict(e);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "CONFLICT"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PERF_001"));
        }
    }

    @Operation(summary = "Get Review by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnterprisePerformanceReviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            EnterprisePerformanceReviewResponse resp = reviewService.getReviewById(user, reviewId);
            return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", resp));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "PERF_404"));
        }
    }

    @Operation(summary = "Get Review Status and Allowed Actions")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status and allowed actions retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PerformanceReviewStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}/status")
    public ResponseEntity<?> getReviewStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> status = reviewService.getReviewStatus(user, reviewId);
            return ResponseEntity.ok(ApiResponse.success("Status retrieved successfully", status));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "PERF_404"));
        }
    }

    @Operation(summary = "Get Cryptographic Snapshot Details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cryptographic snapshot retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PerformanceSnapshotResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}/snapshot")
    public ResponseEntity<?> getSnapshotDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> snapshot = reviewService.getSnapshotDetails(user, reviewId);
            return ResponseEntity.ok(ApiResponse.success("Snapshot retrieved successfully", snapshot));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "PERF_404"));
        }
    }

    @Operation(summary = "Compare Snapshot with Live Dependencies")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Snapshot comparison retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PerformanceSnapshotResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}/snapshot-comparison")
    public ResponseEntity<?> refreshInputsComparison(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> comp = reviewService.refreshInputsComparison(user, reviewId);
            return ResponseEntity.ok(ApiResponse.success("Snapshot comparison retrieved successfully", comp));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "PERF_404"));
        }
    }

    @Operation(summary = "Get Calculation Runs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Calculation runs retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PerformanceCalculationRun.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}/calculation-runs")
    public ResponseEntity<?> getCalculationRuns(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        return ResponseEntity.ok(ApiResponse.success("Calculation runs retrieved successfully", reviewService.getCalculationRuns(user, reviewId)));
    }

    @Operation(summary = "Get Audit Trail")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit trail retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PerformanceReviewAudit.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reviewId}/audit-trail")
    public ResponseEntity<?> getAuditTrail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long reviewId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        return ResponseEntity.ok(ApiResponse.success("Audit trail retrieved successfully", reviewService.getAuditTrail(user, reviewId)));
    }
}
