package com.example.ems.support.controller;

import com.example.ems.auth.service.PermissionRegistry;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.PermissionCheckService;
import com.example.ems.support.dto.*;
import com.example.ems.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.ems.common.dto.ApiResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/support/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin("*")
@Tag(name = "Support Tickets", description = "Ticket Lifecycle, Assignment, Review, and Work Log Operations")
public class SupportTicketController {

    @Autowired
    private SupportTicketService ticketService;

    @Autowired
    private PermissionCheckService permissionCheckService;

    private boolean isNotAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. Create Ticket (POST /api/v1/support/tickets)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Create Support Ticket", description = "Creates a new support ticket in NEW status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ticket created successfully", content = @Content(schema = @Schema(implementation = SupportTicketDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Missing SUPPORT_TICKET_CREATE permission", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateSupportTicketRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_CREATE);
        SupportTicketDetailResponse response = ticketService.createTicket(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. Get Ticket (GET /api/v1/support/tickets/{ticketId})
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Support Ticket Details", description = "Returns support ticket details by ID with SLA, assignment and status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ticket retrieved successfully", content = @Content(schema = @Schema(implementation = SupportTicketDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketById(@PathVariable("ticketId") Long ticketId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        SupportTicketDetailResponse response = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. Ticket List / Filters (GET /api/v1/support/tickets)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Filtered Tickets", description = "Returns paginated list of tickets filtered by status, priority, timeFilter, and specialFilter")
    @GetMapping
    public ResponseEntity<?> getTickets(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "timeFilter", required = false) String timeFilter,
            @RequestParam(name = "specialFilter", required = false) String specialFilter,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "dueDate,asc") String sort) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);

        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Page index must not be less than zero", "PAGE_INVALID"));
        }
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Page size must be between 1 and 100", "SIZE_INVALID"));
        }

        Sort sortObj = Sort.by(Sort.Direction.ASC, "dueDate");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String sortProperty = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sortObj = Sort.by(direction, sortProperty);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<SupportTicketDetailResponse> tickets = ticketService.getTickets(status, priority, timeFilter,
                specialFilter, pageable);
        return ResponseEntity
                .ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4, 5, 6. Manager Review (POST /api/v1/support/tickets/{ticketId}/review)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Manager Review Ticket", description = "Accepts, rejects, or marks ticket as duplicate")
    @PostMapping("/{ticketId}/review")
    public ResponseEntity<?> reviewTicket(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketReviewRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.SUPPORT_TICKET_REVIEW,
                PermissionRegistry.SUPPORT_TICKET_PRIORITY_UPDATE);
        SupportTicketDetailResponse response = ticketService.reviewTicket(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket reviewed successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7, 8. Assign / Reassign Engineer (PUT
    // /api/v1/support/tickets/{ticketId}/assignment)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Assign or Reassign Engineer", description = "Assigns an engineer to the ticket or reassigns to another engineer with a reason")
    @PutMapping("/{ticketId}/assignment")
    public ResponseEntity<?> assignTicket(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketAssignRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requireAnyPermission(
                PermissionRegistry.SUPPORT_TICKET_ASSIGN,
                PermissionRegistry.SUPPORT_TICKET_REASSIGN);
        SupportTicketDetailResponse response = ticketService.assignTicket(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket assigned successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 9. Update Priority (PATCH /api/v1/support/tickets/{ticketId}/priority)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update Ticket Priority", description = "Updates ticket priority and recalculates remaining SLA without resetting elapsed time clock")
    @PatchMapping("/{ticketId}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketPriorityUpdateRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_PRIORITY_UPDATE);
        SupportTicketDetailResponse response = ticketService.updatePriority(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket priority updated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 10. Comments (POST & GET /api/v1/support/tickets/{ticketId}/comments)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Add Comment", description = "Adds a public or internal comment to a support ticket")
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketCommentRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        SupportTicketCommentResponse response = ticketService.addComment(ticketId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @Operation(summary = "Get Comments", description = "Retrieves all comments associated with the ticket")
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<?> getComments(@PathVariable("ticketId") Long ticketId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        List<SupportTicketCommentResponse> response = ticketService.getComments(ticketId);
        return ResponseEntity
                .ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11. Work Logs (POST & GET /api/v1/support/tickets/{ticketId}/work-logs)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Add Work Log", description = "Logs time spent by an engineer on a ticket and updates ticket total actual hours")
    @PostMapping("/{ticketId}/work-logs")
    public ResponseEntity<?> addWorkLog(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportWorkLogRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        SupportWorkLogResponse response = ticketService.addWorkLog(ticketId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work log added successfully", response));
    }

    @Operation(summary = "Get Work Logs", description = "Returns the list of work logs recorded for the ticket")
    @GetMapping("/{ticketId}/work-logs")
    public ResponseEntity<?> getWorkLogs(@PathVariable("ticketId") Long ticketId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        List<SupportWorkLogResponse> response = ticketService.getWorkLogs(ticketId);
        return ResponseEntity
                .ok(ApiResponse.success("Work logs retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 12. Resolve Ticket (POST /api/v1/support/tickets/{ticketId}/resolve)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Resolve Ticket", description = "Marks an IN_PROGRESS ticket as RESOLVED")
    @PostMapping("/{ticketId}/resolve")
    public ResponseEntity<?> resolveTicket(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketResolveRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_RESOLVE);
        SupportTicketDetailResponse response = ticketService.resolveTicket(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket resolved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 13. Close Ticket (POST /api/v1/support/tickets/{ticketId}/close)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Close Ticket", description = "Closes a RESOLVED ticket with optional closure comment")
    @PostMapping("/{ticketId}/close")
    public ResponseEntity<?> closeTicket(
            @PathVariable("ticketId") Long ticketId,
            @RequestBody(required = false) SupportTicketCloseRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_CLOSE);
        SupportTicketDetailResponse response = ticketService.closeTicket(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket closed successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 18. Manual Escalation (POST /api/v1/support/tickets/{ticketId}/escalate)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Manual Ticket Escalation", description = "Manually escalates a ticket, incrementing escalation level")
    @PostMapping("/{ticketId}/escalate")
    public ResponseEntity<?> manualEscalate(
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody SupportTicketEscalateRequest req) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_ESCALATE);
        SupportTicketDetailResponse response = ticketService.manualEscalate(ticketId, req);
        return ResponseEntity
                .ok(ApiResponse.success("Ticket escalated successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 20. Ticket Status History (GET /api/v1/support/tickets/{ticketId}/history)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Ticket Status History", description = "Retrieves status transition history for the ticket")
    @GetMapping("/{ticketId}/history")
    public ResponseEntity<?> getStatusHistory(@PathVariable("ticketId") Long ticketId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        List<SupportStatusHistoryResponse> response = ticketService.getStatusHistory(ticketId);
        return ResponseEntity
                .ok(ApiResponse.success("Status history retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 21. Escalation History (GET /api/v1/support/tickets/{ticketId}/escalations)
    // ─────────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Ticket Escalation History", description = "Retrieves escalation events and actions for the ticket")
    @GetMapping("/{ticketId}/escalations")
    public ResponseEntity<?> getEscalationHistory(@PathVariable("ticketId") Long ticketId) {
        if (isNotAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Full authentication is required.", "AUTH_014"));
        }
        permissionCheckService.requirePermission(PermissionRegistry.SUPPORT_TICKET_VIEW);
        List<SupportEscalationHistoryResponse> response = ticketService.getEscalationHistory(ticketId);
        return ResponseEntity.ok(
                ApiResponse.success("Escalation history retrieved successfully", response));
    }
}
