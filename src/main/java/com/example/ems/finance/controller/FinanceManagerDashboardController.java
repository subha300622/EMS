package com.example.ems.finance.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.finance.dto.FinanceExpenseListItem;
import com.example.ems.finance.dto.manager.FinanceManagerDashboardResponseDto;
import com.example.ems.finance.dto.manager.ManagerPendingActionDto;
import com.example.ems.finance.dto.manager.ManagerTeamMemberDto;
import com.example.ems.finance.service.FinanceManagerDashboardService;
import com.example.ems.security.service.PermissionCheckService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/finance/manager", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Finance Manager Dashboard", description = "Manager Finance Aggregation and Approvals APIs")
public class FinanceManagerDashboardController {

    @Autowired
    private FinanceManagerDashboardService managerDashboardService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    @Operation(
            summary = "Get Manager Finance Dashboard",
            description = "Aggregates team-level finance and workforce information for employees who are within the manager's authorized reporting scope."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Manager finance dashboard retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FinanceManagerDashboardResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Missing finance.team.view permission",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required to access the manager finance dashboard.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_TEAM_VIEW);
        FinanceManagerDashboardResponseDto response = managerDashboardService.getManagerFinanceDashboard();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Team Overview",
            description = "Returns the list of direct reports and workforce finance metrics for the authenticated manager."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerTeamMemberDto.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/team")
    public ResponseEntity<?> getTeam() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_TEAM_VIEW);
        List<ManagerTeamMemberDto> team = managerDashboardService.getManagerTeam();
        return ResponseEntity.ok(team);
    }

    @Operation(
            summary = "Get Team Member Details",
            description = "Returns finance and workforce details for a specific employee within the manager's reporting scope."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team member details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ManagerTeamMemberDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/team/{employeeId}")
    public ResponseEntity<?> getTeamMember(@PathVariable("employeeId") Long employeeId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_TEAM_VIEW);
        try {
            ManagerTeamMemberDto member = managerDashboardService.getManagerTeamMember(employeeId);
            return ResponseEntity.ok(member);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(ex.getMessage(), "AUTH_FORBIDDEN"));
        }
    }

    @Operation(
            summary = "Get Pending Finance Actions",
            description = "Returns pending actionable counts (leave approvals, expense approvals) for the manager's team."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending actions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ManagerPendingActionDto.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/pending-actions")
    public ResponseEntity<?> getPendingActions() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.FINANCE_TEAM_VIEW);
        List<ManagerPendingActionDto> actions = managerDashboardService.getPendingActions();
        return ResponseEntity.ok(actions);
    }

    @Operation(
            summary = "Get Pending Expenses Awaiting Manager Approval",
            description = "Returns all expense claims in pending status for direct reports of the authenticated manager."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending expenses retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FinanceExpenseListItem.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/expenses/pending")
    public ResponseEntity<?> getPendingExpenses() {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.FINANCE_EXPENSE_APPROVE,
                PermissionRegistry.FINANCE_TEAM_VIEW);
        List<FinanceExpenseListItem> expenses = managerDashboardService.getPendingExpenses();
        return ResponseEntity.ok(expenses);
    }

    @Operation(
            summary = "Approve Team Member Expense Claim",
            description = "Approves a pending expense claim belonging to a member of the manager's reporting hierarchy."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense claim approved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/expenses/{expenseId}/approve")
    public ResponseEntity<?> approveExpense(
            @PathVariable("expenseId") Long expenseId,
            @RequestBody(required = false) Map<String, String> body) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        String remarks = body != null && body.containsKey("remarks") ? body.get("remarks") : "Approved by manager";
        try {
            managerDashboardService.approveExpense(expenseId, remarks);
            return ResponseEntity.ok(ApiResponse.success("Expense claim approved successfully", null));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(ex.getMessage(), "AUTH_FORBIDDEN"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(ex.getMessage(), "EXPENSE_NOT_FOUND"));
        }
    }

    @Operation(
            summary = "Reject Team Member Expense Claim",
            description = "Rejects a pending expense claim belonging to a member of the manager's reporting hierarchy."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expense claim rejected successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Expense not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/expenses/{expenseId}/reject")
    public ResponseEntity<?> rejectExpense(
            @PathVariable("expenseId") Long expenseId,
            @RequestBody(required = false) Map<String, String> body) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        String reason = body != null && body.containsKey("reason") ? body.get("reason") : "Rejected by manager";
        try {
            managerDashboardService.rejectExpense(expenseId, reason);
            return ResponseEntity.ok(ApiResponse.success("Expense claim rejected successfully", null));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error(ex.getMessage(), "AUTH_FORBIDDEN"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(ex.getMessage(), "EXPENSE_NOT_FOUND"));
        }
    }
}
