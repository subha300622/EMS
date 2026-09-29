package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.service.FnfSettlementService;
import com.example.ems.performance.dto.PerformanceSnapshotResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/fnf", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Full & Final Settlement (F&F)", description = "F&F Calculation Engine, Approvals, Payment Release & Statement APIs")
public class FnfController {

    @Autowired
    private FnfSettlementService fnfService;

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

    // ==========================================
    // 1. LIFECYCLE ENDPOINTS
    // ==========================================

    @Operation(summary = "Calculate F&F Settlement", description = "Computes pro-rated salary, earnings, deductions, net settlement on the backend and attaches cryptographic snapshot.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F settlement calculated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or calculation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: settlement is immutable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{exitId}/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FNF_CALCULATE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> calculateSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId,
            @Valid @RequestBody FnfCalculationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.calculateSettlement(user, exitId, request);
            return ResponseEntity.ok(ApiResponse.success("F&F settlement calculated successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "FNF_IMMUTABLE"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    @Operation(summary = "Recalculate F&F Settlement", description = "Recalculates settlement with new line items. Blocked if SUBMITTED or beyond.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F settlement recalculated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or calculation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: settlement is immutable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{fnfId}/recalculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FNF_CALCULATE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> recalculateSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId,
            @Valid @RequestBody FnfCalculationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.recalculateSettlement(user, fnfId, request);
            return ResponseEntity.ok(ApiResponse.success("F&F settlement recalculated successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "FNF_IMMUTABLE"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_001"));
        }
    }

    @Operation(summary = "Submit F&F Settlement for Approval", description = "Transitions settlement from CALCULATED to SUBMITTED and initiates the multi-stage approval workflow.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement submitted for approval successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or submit error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: settlement is immutable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{fnfId}/submit")
    @PreAuthorize("hasAuthority('FNF_CALCULATE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> submitSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.submitSettlement(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Settlement submitted for approval successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "FNF_IMMUTABLE"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_SUBMIT_ERROR"));
        }
    }

    @Operation(summary = "Cancel F&F Settlement", description = "Cancels active settlement before approvals are completed.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F settlement cancelled successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or cancel error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{fnfId}/cancel")
    @PreAuthorize("hasAuthority('FNF_CALCULATE') or hasRole('FINANCE') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> cancelSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId,
            @RequestParam(required = false) String reason) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.cancelSettlement(user, fnfId, reason);
            return ResponseEntity.ok(ApiResponse.success("F&F settlement cancelled successfully", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_CANCEL_ERROR"));
        }
    }

    @Operation(summary = "Get F&F Settlement Details", description = "Retrieves itemized earnings, deductions, net settlement, and processing status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement details retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.getSettlementById(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Settlement details retrieved successfully", resp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Settlement by Exit ID", description = "Retrieves settlement for a specific employee exit.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement details retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/exit/{exitId}")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSettlementByExit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.getSettlementByExitId(user, exitId);
            return ResponseEntity.ok(ApiResponse.success("Settlement details retrieved successfully", resp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Settlements for Employee", description = "Retrieves settlements for an employee.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee settlements retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FnfCalculationResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlements not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSettlementsForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            List<FnfCalculationResponse> list = fnfService.getSettlementsForEmployee(user, employeeId);
            return ResponseEntity.ok(ApiResponse.success("Employee settlements retrieved successfully", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get F&F Status & Allowed Actions", description = "Retrieves current status, metadata, and next permissible actions.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settlement status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfSettlementStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/status")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSettlementStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> status = fnfService.getSettlementStatus(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Settlement status retrieved successfully", status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Update F&F Settlement", description = "Recalculates or updates line items. Blocked if SUBMITTED or beyond.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F settlement updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or update error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: settlement is immutable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/{fnfId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FNF_CALCULATE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId,
            @Valid @RequestBody FnfCalculationRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.updateSettlement(user, fnfId, request);
            return ResponseEntity.ok(ApiResponse.success("F&F settlement updated successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "FNF_IMMUTABLE"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_003"));
        }
    }

    // ==========================================
    // 2. FINANCIAL BREAKDOWN ENDPOINTS
    // ==========================================

    @Operation(summary = "Get Itemized Financial Breakdown", description = "Returns itemized earnings and deductions breakdown.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Breakdown retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/breakdown")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getBreakdown(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> breakdown = fnfService.getFinancialBreakdown(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Breakdown retrieved successfully", breakdown));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Earnings Breakdown", description = "Returns itemized earnings breakdown.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Earnings retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfEarningsBreakdown.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/earnings")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getEarnings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.getSettlementById(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Earnings retrieved successfully", resp.getEarnings()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Deductions Breakdown", description = "Returns itemized deductions breakdown.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deductions retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfDeductionsBreakdown.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/deductions")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getDeductions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfCalculationResponse resp = fnfService.getSettlementById(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Deductions retrieved successfully", resp.getDeductions()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    // ==========================================
    // 3. SNAPSHOTS & IMMUTABILITY ENDPOINTS
    // ==========================================

    @Operation(summary = "Get Cryptographic Snapshot", description = "Returns the lean calculation snapshot, version, canonical SHA-256 hash, and integrity check.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Snapshot retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PerformanceSnapshotResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Snapshot not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/snapshot")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSnapshot(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> snapshot = fnfService.getSnapshotDetails(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Snapshot retrieved successfully", snapshot));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Lean Calculation Inputs", description = "Returns the structured lean calculation inputs.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inputs retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PerformanceSnapshotResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Inputs not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/inputs")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getInputs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        return getSnapshot(authHeader, fnfId);
    }

    @Operation(summary = "Refresh & Compare Live Inputs", description = "Checks whether live dependencies (salary, leave, expense, asset) have changed since submission without modifying stored snapshot.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inputs comparison completed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfReadinessDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{fnfId}/refresh-inputs")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> refreshInputs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> comp = fnfService.refreshInputsComparison(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Inputs comparison completed", comp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    // ==========================================
    // 4. READINESS & APPROVAL ENDPOINTS
    // ==========================================

    @Operation(summary = "Get Clearance Status", description = "Checks clearance status across all departments.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfReadinessDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/clearance-status")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getClearanceStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfReadinessDto readiness = fnfService.getClearanceStatus(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Clearance status retrieved successfully", readiness));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get Blocking Items", description = "Lists any blocking items preventing submission or payment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blocking items retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FnfBlockingItemDto.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/blocking-items")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getBlockingItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            List<FnfBlockingItemDto> blockers = fnfService.getBlockingItems(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Blocking items retrieved successfully", blockers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Check Approval Eligibility", description = "Verifies if settlement satisfies clearance and calculation conditions for submission.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Approval eligibility verified",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfReadinessDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/approval-eligibility")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> checkApprovalEligibility(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfReadinessDto readiness = fnfService.checkApprovalEligibility(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Approval eligibility verified", readiness));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    @Operation(summary = "Get F&F Approvals Progress", description = "Retrieves the 3-stage approval progress (Finance Manager -> HR Manager -> Company Admin).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F approvals retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfApprovalsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Approvals not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/approvals")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getApprovals(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfApprovalsResponse resp = fnfService.getApprovalsForSettlement(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("F&F approvals retrieved successfully", resp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_004"));
        }
    }

    // ==========================================
    // 5. PAYMENT HARDENING & IDEMPOTENCY ENDPOINTS
    // ==========================================

    @Operation(summary = "Validate Payment Readiness", description = "Pre-validates that all approval stages, bank accounts, and clearances are satisfied before payment.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment readiness validated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfReadinessDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Payment readiness conflict",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequestMapping(value = "/{fnfId}/payment/validate", method = {RequestMethod.GET, RequestMethod.POST})
    @PreAuthorize("hasAuthority('FNF_PAYMENT') or hasRole('FINANCE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> validatePaymentReadiness(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfReadinessDto readiness = fnfService.validatePaymentReadiness(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Payment readiness validated", readiness));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "PAYMENT_NOT_READY"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAYMENT_NOT_READY"));
        }
    }

    @Operation(summary = "Release F&F Payment", description = "Disburses payment with pessimistic row lock, idempotency conflict checks, and transitions status to PAYMENT_RELEASED.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F payment released successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfPaymentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or payment processing error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: idempotency key reused or already released",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{fnfId}/payment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FNF_PAYMENT') or hasRole('FINANCE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> processPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @PathVariable Long fnfId,
            @Valid @RequestBody FnfPaymentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            String trimmedHeader = idempotencyKeyHeader.trim();
            if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()
                    && !trimmedHeader.equals(request.getIdempotencyKey().trim())) {
                return ResponseEntity.badRequest().body(ErrorResponse.error(
                        "Conflicting idempotency keys provided in Idempotency-Key header ('" + trimmedHeader + "') and request body ('" + request.getIdempotencyKey() + "').",
                        "FNF_008"
                ));
            }
            request.setIdempotencyKey(trimmedHeader);
        }

        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Idempotency-Key is required for payment disbursement.", "FNF_009"));
        }

        try {
            FnfPaymentResponse resp = fnfService.processPayment(user, fnfId, request);
            return ResponseEntity.ok(ApiResponse.success("F&F payment released successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "IDEMPOTENCY_KEY_REUSED"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_005"));
        }
    }

    @Operation(summary = "Retry F&F Payment", description = "Retries a previously failed payment attempt. Strictly rejected if already PAYMENT_RELEASED or FINALIZED.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retry executed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfPaymentResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or retry error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: payment already released",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{fnfId}/payment/retry", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('FNF_PAYMENT') or hasRole('FINANCE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> retryPayment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId,
            @Valid @RequestBody FnfPaymentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfPaymentResponse resp = fnfService.retryPayment(user, fnfId, request);
            return ResponseEntity.ok(ApiResponse.success("Payment retry executed successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "PAYMENT_ALREADY_RELEASED"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "PAYMENT_RETRY_ERROR"));
        }
    }

    // ==========================================
    // 6. FINALIZATION & TERMINAL LOCK ENDPOINTS
    // ==========================================

    @Operation(summary = "Finalize Settlement", description = "Formally finalizes the settlement, permanently locking all financial attributes.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F settlement finalized successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfSettlementStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request or finalization error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: settlement is already immutable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{fnfId}/finalize")
    @PreAuthorize("hasAuthority('FNF_FINALIZE') or hasRole('FINANCE') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> finalizeSettlement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> resp = fnfService.finalizeSettlement(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("F&F settlement finalized successfully", resp));
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.error(e.getMessage(), "FNF_IMMUTABLE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_FINALIZE_ERROR"));
        }
    }

    @Operation(summary = "Get Finalization Status", description = "Checks finalization checklist and readiness.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Finalization status retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfReadinessDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Settlement not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/finalization-status")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getFinalizationStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            FnfReadinessDto readiness = fnfService.getFinalizationStatus(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("Finalization status retrieved successfully", readiness));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_002"));
        }
    }

    // ==========================================
    // 7. DOCUMENTS ENDPOINTS
    // ==========================================

    @Operation(summary = "Get F&F Settlement Documents", description = "Retrieves generated F&F settlement statements, clearance slips, and experience letters.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F documents retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FnfDocumentResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documents not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fnfId}/documents")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('EMPLOYEE') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            List<FnfDocumentResponse> docs = fnfService.getSettlementDocuments(user, fnfId);
            return ResponseEntity.ok(ApiResponse.success("F&F documents retrieved successfully", docs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "FNF_006"));
        }
    }

    @Operation(summary = "Generate F&F Settlement Documents", description = "Generates PDF settlement statements and clearance sign-offs.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F documents generated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = FnfDocumentResponse.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Documents generation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{fnfId}/documents/generate")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> generateDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long fnfId) {
        return getDocuments(authHeader, fnfId);
    }

    // ==========================================
    // 8. DASHBOARD ENDPOINT
    // ==========================================

    @Operation(summary = "Get F&F Operational Dashboard", description = "Retrieves aggregate counts across settlement statuses and amounts.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "F&F dashboard retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FnfDashboardSummaryResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dashboard summary error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('FNF_VIEW') or hasRole('FINANCE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            Map<String, Object> summary = fnfService.getDashboardSummary(user);
            return ResponseEntity.ok(ApiResponse.success("F&F dashboard retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "FNF_DASHBOARD_ERROR"));
        }
    }
}

