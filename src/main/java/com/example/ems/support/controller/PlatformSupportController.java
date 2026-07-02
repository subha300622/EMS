package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.*;
import com.example.ems.support.service.PlatformSupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/support")
@CrossOrigin("*")
@Tag(name = "Platform Support Administration")
public class PlatformSupportController {

    @Autowired
    private PlatformSupportService supportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    // ── AUTH HELPERS ─────────────────────────────────────────────────────────
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

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission) || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Missing " + permission, "AUTH_003"));
    }

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    @Operation(summary = "Get Support Dashboard Statistics")
    public ResponseEntity<?> getDashboard(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            PlatformSupportDashboardResponse stats = supportService.getDashboardData();
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_001"));
        }
    }

    // ── 2. SEARCH & FILTER ───────────────────────────────────────────────────
    @GetMapping("/search")
    @Operation(summary = "Search and Filter Support Tickets")
    public ResponseEntity<?> searchTickets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ticketNumber,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long business, // businessId
            @RequestParam(required = false) String assignedAgent,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Boolean hasAttachment,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, limit, sort);
            Page<MySupportTicket> ticketPage = supportService.searchTickets(
                    q, ticketNumber, subject, status, priority, categoryId, business, assignedAgent, createdBy,
                    dateFrom, dateTo, hasAttachment, slaBreached, pageable);

            List<MyTicketsResponse.TicketListItem> items = ticketPage.getContent().stream()
                    .map(t -> new MyTicketsResponse.TicketListItem(
                            t.getId(), t.getTicketNumber(), t.getSubject(),
                            t.getCategory() != null ? t.getCategory().getName() : null,
                            t.getPriority().name(), t.getStatus().name(), t.getAssignedTeam(),
                            t.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            t.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .collect(Collectors.toList());

            MyTicketsResponse myResp = new MyTicketsResponse(items, new MyTicketsResponse.PaginationDto(
                    ticketPage.getNumber(), ticketPage.getSize(), ticketPage.getTotalElements(), ticketPage.getTotalPages(), ticketPage.hasNext()));

            return ResponseEntity.ok(ApiResponse.success("Search results retrieved", myResp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_002"));
        }
    }

    // ── 3. TICKET RETRIEVAL & CRUD ───────────────────────────────────────────
    @GetMapping("/tickets/{ticketId}")
    @Operation(summary = "Get Ticket Details")
    public ResponseEntity<?> getTicketDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            PlatformSupportTicketDetailResponse detail = supportService.getTicketDetails(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", detail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_003"));
        }
    }

    @PostMapping("/tickets")
    @Operation(summary = "Create Ticket on Behalf of Business")
    public ResponseEntity<?> createTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformCreateTicketRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            MySupportTicket ticket = supportService.createTicket(req, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Support ticket created successfully", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_004"));
        }
    }

    @PutMapping("/tickets/{ticketId}")
    @Operation(summary = "Update Ticket Priority, Status, or Category")
    public ResponseEntity<?> updateTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId,
            @RequestBody PlatformUpdateTicketRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            MySupportTicket ticket = supportService.updateTicket(ticketId, req, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_005"));
        }
    }

    @PatchMapping("/tickets/{ticketId}/assign")
    @Operation(summary = "Assign Agent to Ticket")
    public ResponseEntity<?> assignTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId,
            @Valid @RequestBody PlatformAssignAgentRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            MySupportTicket ticket = supportService.assignTicket(ticketId, req, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_006"));
        }
    }

    @PatchMapping("/tickets/{ticketId}/status")
    @Operation(summary = "Update Ticket Status")
    public ResponseEntity<?> updateStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId,
            @RequestParam String status) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            MySupportTicket ticket = supportService.changeStatus(ticketId, status, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Ticket status changed to " + status, ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_007"));
        }
    }

    @DeleteMapping("/tickets/{ticketId}")
    @Operation(summary = "Soft Delete Ticket")
    public ResponseEntity<?> deleteTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.softDeleteTicket(ticketId, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Ticket soft deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_008"));
        }
    }

    // ── 4. MESSAGES & REPLIES & ATTACHMENTS ─────────────────────────────────
    @PostMapping("/tickets/{ticketId}/messages")
    @Operation(summary = "Post Reply or Internal Note")
    public ResponseEntity<?> addMessage(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId,
            @Valid @RequestBody PlatformMessageRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.reply")) return forbiddenResponse("support.reply");

        try {
            MySupportComment comment = supportService.addComment(ticketId, req, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message posted successfully", comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_009"));
        }
    }

    @PostMapping(value = "/tickets/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Attachment on Ticket")
    public ResponseEntity<?> uploadAttachment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.reply")) return forbiddenResponse("support.reply");

        try {
            MySupportAttachment att = supportService.uploadAttachment(ticketId, file, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("File attached successfully", att));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_010"));
        }
    }

    @GetMapping("/tickets/{ticketId}/activities")
    @Operation(summary = "Get Ticket Activities Timeline")
    public ResponseEntity<?> getTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long ticketId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            List<MySupportTimelineActivity> list = supportService.getTimelineActivities(ticketId);
            return ResponseEntity.ok(ApiResponse.success("Timeline activities retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_011"));
        }
    }

    // ── 5. BULK OPERATIONS ───────────────────────────────────────────────────
    @PostMapping("/bulk/status")
    @Operation(summary = "Bulk Status Update")
    public ResponseEntity<?> bulkStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformBulkActionRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.bulkStatusChange(req.getTicketIds(), req.getStatus(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bulk status changed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_012"));
        }
    }

    @PostMapping("/bulk/priority")
    @Operation(summary = "Bulk Priority Update")
    public ResponseEntity<?> bulkPriority(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformBulkActionRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.bulkPriorityChange(req.getTicketIds(), req.getPriority(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bulk priorities updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_013"));
        }
    }

    @PostMapping("/bulk/assign")
    @Operation(summary = "Bulk Ticket Assignment")
    public ResponseEntity<?> bulkAssign(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformBulkActionRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.bulkAssign(req.getTicketIds(), req.getAgentId(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bulk tickets assigned successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_014"));
        }
    }

    @PostMapping("/bulk/close")
    @Operation(summary = "Bulk Ticket Close")
    public ResponseEntity<?> bulkClose(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformBulkActionRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.bulkClose(req.getTicketIds(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bulk tickets closed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_015"));
        }
    }

    @PostMapping("/bulk/delete")
    @Operation(summary = "Bulk Ticket Soft Delete")
    public ResponseEntity<?> bulkDelete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformBulkActionRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.bulkDelete(req.getTicketIds(), user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Bulk tickets soft deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_016"));
        }
    }

    // ── 6. AGENT STATUS & REALTIME availability ─────────────────────────────
    @GetMapping("/agents")
    @Operation(summary = "List Active Support Agents workload")
    public ResponseEntity<?> listAgents(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            List<PlatformAgentResponse> list = supportService.listAgents();
            return ResponseEntity.ok(ApiResponse.success("Active support agents retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_017"));
        }
    }

    @PutMapping("/agents/{agentId}/status")
    @Operation(summary = "Set Support Agent Availability Status (ONLINE/OFFLINE)")
    public ResponseEntity<?> updateAgentStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long agentId,
            @RequestParam String status) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            PlatformAgentResponse updated = supportService.updateAgentStatus(agentId, status);
            return ResponseEntity.ok(ApiResponse.success("Agent status changed successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_018"));
        }
    }

    // ── 7. SLA CONFIGURATIONS ────────────────────────────────────────────────
    @GetMapping("/sla")
    @Operation(summary = "Get SLA configurations")
    public ResponseEntity<?> getSlaList(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            List<SupportSla> list = supportService.listSlas();
            return ResponseEntity.ok(ApiResponse.success("SLA configurations retrieved", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_019"));
        }
    }

    @PutMapping("/sla/{priority}")
    @Operation(summary = "Update SLA parameters for a priority level")
    public ResponseEntity<?> updateSla(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String priority,
            @Valid @RequestBody SupportSlaRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportSla updated = supportService.updateSla(priority, req);
            return ResponseEntity.ok(ApiResponse.success("SLA config updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_020"));
        }
    }

    // ── 8. SAVED REPLIES / TEMPLATES ──────────────────────────────────────────
    @GetMapping("/templates")
    @Operation(summary = "List Saved replies")
    public ResponseEntity<?> getTemplates(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            List<SupportTemplate> list = supportService.listTemplates();
            return ResponseEntity.ok(ApiResponse.success("Support templates list", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_021"));
        }
    }

    @PostMapping("/templates")
    @Operation(summary = "Create Saved Reply template")
    public ResponseEntity<?> createTemplate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody SupportTemplateRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportTemplate t = supportService.createTemplate(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Saved reply template created", t));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_022"));
        }
    }

    @PutMapping("/templates/{templateId}")
    @Operation(summary = "Update Template details")
    public ResponseEntity<?> updateTemplate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long templateId,
            @Valid @RequestBody SupportTemplateRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            SupportTemplate t = supportService.updateTemplate(templateId, req);
            return ResponseEntity.ok(ApiResponse.success("Template updated", t));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_023"));
        }
    }

    @DeleteMapping("/templates/{templateId}")
    @Operation(summary = "Delete Saved Reply Template")
    public ResponseEntity<?> deleteTemplate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long templateId) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            supportService.deleteTemplate(templateId);
            return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_024"));
        }
    }

    // ── 9. TICKET MERGING ────────────────────────────────────────────────────
    @PostMapping("/tickets/merge")
    @Operation(summary = "Merge duplicate tickets into a primary ticket")
    public ResponseEntity<?> mergeTickets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody MergeTicketsRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            MySupportTicket primary = supportService.mergeTickets(req, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Tickets merged successfully", primary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_025"));
        }
    }

    // ── 10. SEGMENTED ANALYTICS ──────────────────────────────────────────────
    @GetMapping("/analytics/overview")
    @Operation(summary = "Get high-level overview metrics")
    public ResponseEntity<?> getAnalyticsOverview(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Analytics overview", supportService.getAnalyticsOverview()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_026"));
        }
    }

    @GetMapping("/analytics/agents")
    @Operation(summary = "Get performance by agent")
    public ResponseEntity<?> getAnalyticsAgents(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Analytics agents", supportService.getAnalyticsAgents()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_027"));
        }
    }

    @GetMapping("/analytics/businesses")
    @Operation(summary = "Get volume by business")
    public ResponseEntity<?> getAnalyticsBusinesses(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Analytics businesses", supportService.getAnalyticsBusinesses()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_028"));
        }
    }

    @GetMapping("/analytics/categories")
    @Operation(summary = "Get volume by category")
    public ResponseEntity<?> getAnalyticsCategories(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Analytics categories", supportService.getAnalyticsCategories()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_029"));
        }
    }

    @GetMapping("/analytics/sla")
    @Operation(summary = "Get SLA compliance statistics")
    public ResponseEntity<?> getAnalyticsSla(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.view")) return forbiddenResponse("support.view");

        try {
            return ResponseEntity.ok(ApiResponse.success("Analytics SLA", supportService.getAnalyticsSla()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_030"));
        }
    }

    // ── 11. REPORTS EXPORT ───────────────────────────────────────────────────
    @GetMapping("/reports/export")
    @Operation(summary = "Export Support Reports CSV")
    public ResponseEntity<?> exportReport(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long business, // businessId
            @RequestParam(required = false) String agent) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.manage")) return forbiddenResponse("support.manage");

        try {
            String csv = supportService.compileReportCsv(from, to, category, priority, status, business, agent);
            byte[] csvBytes = csv.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("support_report_" + System.currentTimeMillis() + ".csv")
                    .build());

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_031"));
        }
    }
}
