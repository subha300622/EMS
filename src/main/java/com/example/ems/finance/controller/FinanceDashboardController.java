package com.example.ems.finance.controller;

import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.dto.FinanceDashboardResponseDto;
import com.example.ems.finance.service.FinanceDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/finance", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Finance Dashboard", description = "Finance Center Aggregation APIs for employee self-service")
public class FinanceDashboardController {

    @Autowired
    private FinanceDashboardService financeDashboardService;

    @Operation(
            summary = "Get Finance Center Dashboard",
            description = "Aggregates employee attendance, leave balances, CTC compensation, performance rating, pending actions, today's schedule, and finance team directory into a single response payload."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Finance dashboard data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FinanceDashboardResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee profile not found for authenticated user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required to access the finance dashboard.", "AUTH_014"));
        }
        FinanceDashboardResponseDto response = financeDashboardService.getFinanceDashboard();
        return ResponseEntity.ok(response);
    }
}
