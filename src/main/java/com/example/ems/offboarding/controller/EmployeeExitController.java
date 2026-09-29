package com.example.ems.offboarding.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.*;
import com.example.ems.offboarding.service.EmployeeExitService;
import com.example.ems.offboarding.service.ExitClearanceService;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/employee-exits", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Employee Exit & Offboarding", description = "Exit Request Lifecycle, Clearance Assignment & Clearance Execution APIs")
public class EmployeeExitController {

    @Autowired
    private EmployeeExitService exitService;

    @Autowired
    private ExitClearanceService clearanceService;

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

    @Operation(summary = "Create Exit Request", description = "Submits a formal exit request/resignation, assigning reporting manager approval workflow.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Exit request submitted successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation or duplicate exit error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('EXIT_CREATE') or hasRole('EMPLOYEE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createExit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateExitRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            ExitResponse resp = exitService.createExitRequest(user, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Exit request created successfully", resp));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EXIT_001"));
        }
    }

    @Operation(summary = "Get Exit Request Details", description = "Retrieves full details of an exit request including clearance checklist status.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit details retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Exit request not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EXIT_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getExitById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            ExitDetailResponse resp = exitService.getExitById(user, id);
            return ResponseEntity.ok(ApiResponse.success("Exit details retrieved successfully", resp));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "EXIT_002"));
        }
    }

    @Operation(summary = "List Exit Requests", description = "Retrieves a paginated list of exit requests with status and keyword filtering.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit requests retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ExitResponse.class))))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('EXIT_VIEW') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getExits(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ExitResponse> results = exitService.getExits(user, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Exit requests retrieved successfully", results));
    }

    @Operation(summary = "Initiate HR Offboarding", description = "HR confirms last working date and instantiates the 4 department clearance tasks (IT, Admin, Finance, Manager).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "HR offboarding initiated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OffboardingInitiationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or manager approval pending", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = {"/{exitId}/offboarding", "/{exitId}/offboard"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CLEARANCE_MANAGE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> initiateOffboarding(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId,
            @Valid @RequestBody HrOffboardingRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            OffboardingInitiationResponse resp = exitService.initiateOffboarding(user, exitId, request);
            return ResponseEntity.ok(ApiResponse.success("HR offboarding initiated successfully", resp));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EXIT_003"));
        }
    }

    @Operation(summary = "Get Department Clearances", description = "Retrieves the status, assignee, and checklist items for all 4 clearances of an exit process.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearances retrieved successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ExitClearanceDto.class))))
    })
    @GetMapping("/{exitId}/clearances")
    @PreAuthorize("hasAuthority('CLEARANCE_VIEW') or hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getClearances(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            List<ExitClearanceDto> list = clearanceService.getClearancesForExit(user, exitId);
            return ResponseEntity.ok(ApiResponse.success("Clearances retrieved successfully", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "EXIT_004"));
        }
    }

    @Operation(summary = "Assign / Reassign Clearance Task", description = "Assigns an active user and updates clearance instructions/reasons for a clearance record.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance assigned successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitClearanceDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid assignee or request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = {"/{exitId}/clearances/{clearanceId}", "/{exitId}/clearances/{clearanceId}/assign"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CLEARANCE_MANAGE') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateClearanceAssignment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId,
            @PathVariable Long clearanceId,
            @Valid @RequestBody ClearanceAssignmentRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            ExitClearanceDto resp = clearanceService.updateClearanceAssignment(user, exitId, clearanceId, request);
            return ResponseEntity.ok(ApiResponse.success("Clearance assignment updated successfully", resp));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CLR_001"));
        }
    }

    @Operation(summary = "Execute Clearance Action", description = "Executes an action (CLEAR, HOLD, REJECT) on a department clearance with verification details.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clearance action executed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExitClearanceDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid action or request payload", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{exitId}/clearances/{clearanceId}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('CLEARANCE_ACTION') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> executeClearanceAction(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long exitId,
            @PathVariable Long clearanceId,
            @Valid @RequestBody ClearanceActionRequest request) {
        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));

        try {
            ExitClearanceDto resp = clearanceService.processClearanceAction(user, exitId, clearanceId, request);
            return ResponseEntity.ok(ApiResponse.success("Clearance action processed successfully", resp));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CLR_002"));
        }
    }
}
