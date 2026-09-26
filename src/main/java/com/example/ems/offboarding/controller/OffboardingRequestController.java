package com.example.ems.offboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.offboarding.dto.InitiateExitRequest;
import com.example.ems.offboarding.dto.InitiateExitResponse;
import com.example.ems.offboarding.dto.OffboardingAnalyticsResponse;
import com.example.ems.offboarding.dto.OffboardingRequestSummaryDto;
import com.example.ems.offboarding.service.OffboardingDashboardService;
import com.example.ems.offboarding.service.OffboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/offboarding", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Offboarding Dashboard", description = "Offboarding Dashboard, Exit Requests & Analytics APIs")
public class OffboardingRequestController {

    @Autowired
    private OffboardingDashboardService dashboardService;

    @Autowired
    private OffboardingService offboardingService;

    @Operation(summary = "Get Offboarding Requests", description = "Retrieves a paginated list of exit and offboarding requests for the dashboard with status and search filtering")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Offboarding requests retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = OffboardingRequestSummaryDto.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination or query parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('offboarding.request.read') or hasAuthority('OFFBOARDING_VIEW') or hasAuthority('EXIT_VIEW') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<OffboardingRequestSummaryDto>>> getRequests(
            @Parameter(description = "Filter by dashboard status: ACTIVE, COMPLETED, SCHEDULED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by employee name or employee code")
            @RequestParam(required = false) String search,
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)")
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Page index cannot be negative", "VAL_001"));
        }
        if (size <= 0 || size > 100) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Page size must be between 1 and 100", "VAL_001"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OffboardingRequestSummaryDto> result = dashboardService.getRequests(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Offboarding requests retrieved successfully", result));
    }

    @Operation(summary = "Get Exit Analytics", description = "Retrieves aggregated offboarding KPI metrics and statistics with optional date filtering")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Exit analytics retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OffboardingAnalyticsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('offboarding.analytics.read') or hasAuthority('OFFBOARDING_VIEW') or hasAuthority('EXIT_VIEW') or hasRole('HR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OffboardingAnalyticsResponse>> getAnalytics(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Start date ('from') must not be after end date ('to')", "VAL_001"));
        }

        OffboardingAnalyticsResponse analytics = dashboardService.getAnalytics(from, to);
        return ResponseEntity.ok(ApiResponse.success("Exit analytics retrieved successfully", analytics));
    }

    @Operation(summary = "Initiate Offboarding Exit", description = "Initiates an offboarding exit request for an employee with automatic template matching and task instantiation.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Offboarding exit initiated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InitiateExitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Active offboarding already exists for employee",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/requests", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('offboarding.request.create')")
    public ResponseEntity<ApiResponse<InitiateExitResponse>> initiateExit(
            @Valid @RequestBody InitiateExitRequest request) {
        InitiateExitResponse response = offboardingService.initiateExit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Offboarding exit initiated successfully", response));
    }

    @Operation(summary = "Assign Template to Exit Request", description = "Explicitly assigns an offboarding template to an exit request and generates frozen checklist tasks.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template assigned to request successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InitiateExitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid template ID or request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Offboarding request or template not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/requests/{requestId}/template", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('offboarding.template.manage') or hasAuthority('offboarding.request.create')")
    public ResponseEntity<ApiResponse<InitiateExitResponse>> assignTemplateToRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody com.example.ems.offboarding.dto.AssignTemplateToRequestDto request) {
        InitiateExitResponse response = offboardingService.assignTemplateToRequest(requestId, request);
        return ResponseEntity.ok(ApiResponse.success("Template assigned to exit request successfully", response));
    }
}
