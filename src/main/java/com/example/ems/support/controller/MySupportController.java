package com.example.ems.support.controller;
import java.util.List;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.MySupportAttachment;
import com.example.ems.support.service.MySupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/my-support")
@CrossOrigin("*")
public class MySupportController {

    @Autowired
    private MySupportService supportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    // ── Auth helpers ──────────────────────────────────────────────────────────

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
        if (roleService.hasPermission(user.getWorkEmail(), permission) || roleService.isSuperAdmin(user.getWorkEmail()))
            return true;
        if ("support.self.read".equals(permission))
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.read");
        if ("support.self.create".equals(permission))
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.create");
        if ("support.self.comment.create".equals(permission))
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.update");
        if ("support.self.close".equals(permission))
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.update");
        return false;
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAG: My Support - Tickets
    // ═══════════════════════════════════════════════════════════════════════════

    @Tag(name = "My Support")
    @Operation(summary = "Get My Support Dashboard")
    @GetMapping("/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<SupportDashboardResponse>> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully",
                    supportService.getDashboard(currentUser.getWorkEmail())));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Create Support Ticket")
    @PostMapping("/tickets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<CreateTicketResponse>> createTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateTicketRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Support ticket created successfully",
                            supportService.createTicket(currentUser.getWorkEmail(), request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Get My Tickets (paginated/filtered by status, priority, categoryId, search)")
    @GetMapping("/tickets")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<MyTicketsResponse>> getMyTickets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            return ResponseEntity.ok(ApiResponse.success("Tickets list retrieved successfully",
                    supportService.getMyTickets(currentUser.getWorkEmail(), status, priority, categoryId, search, pageable)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Get Ticket Details")
    @GetMapping("/tickets/{ticketId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<TicketDetailsResponse>> getTicketDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully",
                    supportService.getTicketDetails(currentUser.getWorkEmail(), ticketId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Update Ticket (subject, description, priority) — only OPEN tickets")
    @PutMapping("/tickets/{ticketId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<UpdateTicketResponse>> updateTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @RequestBody UpdateTicketRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully",
                    supportService.updateTicket(currentUser.getWorkEmail(), ticketId, request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Close Ticket (with optional rating + feedback)")
    @PatchMapping("/tickets/{ticketId}/close")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<CloseTicketResponse>> closeTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody CloseTicketRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.close")) return (ResponseEntity) forbiddenResponse("support.self.close");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket closed successfully",
                    supportService.closeTicket(currentUser.getWorkEmail(), ticketId, request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Reopen a CLOSED ticket")
    @PatchMapping("/tickets/{ticketId}/reopen")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<ReopenTicketResponse>> reopenTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket reopened successfully",
                    supportService.reopenTicket(currentUser.getWorkEmail(), ticketId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Escalate Ticket to CRITICAL priority")
    @PatchMapping("/tickets/{ticketId}/escalate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<EscalateTicketResponse>> escalateTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody EscalateTicketRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket escalated successfully",
                    supportService.escalateTicket(currentUser.getWorkEmail(), ticketId, request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Delete an OPEN ticket")
    @DeleteMapping("/tickets/{ticketId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            supportService.deleteTicket(currentUser.getWorkEmail(), ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully"));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Get Ticket Activity Timeline")
    @GetMapping("/tickets/{ticketId}/timeline")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<TicketTimelineResponse>> getTicketTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Ticket timeline retrieved successfully",
                    supportService.getTicketTimeline(currentUser.getWorkEmail(), ticketId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Submit satisfaction feedback for a RESOLVED or CLOSED ticket")
    @PostMapping("/tickets/{ticketId}/feedback")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<TicketFeedbackResponse>> submitFeedback(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody TicketFeedbackRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully",
                    supportService.submitFeedback(currentUser.getWorkEmail(), ticketId, request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAG: My Support - Comments
    // ═══════════════════════════════════════════════════════════════════════════

    @Tag(name = "My Support")
    @Operation(summary = "Get All Comments on a Ticket")
    @GetMapping("/tickets/{ticketId}/comments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<GetCommentsResponse>> getComments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully",
                    supportService.getComments(currentUser.getWorkEmail(), ticketId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Add a Comment to a Ticket")
    @PostMapping("/tickets/{ticketId}/comments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AddCommentResponse>> addComment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody AddCommentRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.comment.create")) return (ResponseEntity) forbiddenResponse("support.self.comment.create");
        try {
            return ResponseEntity.ok(ApiResponse.success("Comment added successfully",
                    supportService.addComment(currentUser.getWorkEmail(), ticketId, request)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAG: My Support - Attachments
    // ═══════════════════════════════════════════════════════════════════════════

    @Tag(name = "My Support")
    @Operation(summary = "Upload an Attachment (returns fileId to attach to ticket/comment)")
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AttachmentUploadResponse>> uploadAttachment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return (ResponseEntity) forbiddenResponse("support.self.create");
        try {
            return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully",
                    supportService.uploadAttachment(file)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Download an Attachment by fileId")
    @GetMapping("/attachments/{attachmentId}/download")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<?> downloadAttachment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("attachmentId") String attachmentId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");
        try {
            MySupportAttachment att = supportService.getAttachmentForDownload(currentUser.getWorkEmail(), attachmentId);
            String contentType = att.getFileType() != null ? att.getFileType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(att.getFileName()).build().toString())
                    .body(att.getFileContent());
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAG: My Support - Knowledge Base
    // ═══════════════════════════════════════════════════════════════════════════

    @Tag(name = "My Support")
    @Operation(summary = "Get Support Categories (with subcategories)")
    @GetMapping("/categories")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<SupportCategoryResponse>>> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Support categories retrieved successfully",
                    supportService.getCategories()));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Search Knowledge Base (legacy — use /knowledge-base/articles instead)")
    @GetMapping("/knowledge-base")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FAQSearchResponse>> searchFAQ(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Knowledge base articles retrieved successfully",
                    supportService.searchFAQ(keyword)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "List Knowledge Base Articles (optional ?category= filter)")
    @GetMapping("/knowledge-base/articles")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FAQSearchResponse>> getKnowledgeBaseArticles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "category", required = false) String category){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Knowledge base articles retrieved successfully",
                    supportService.getKnowledgeBaseArticles(category)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    @Tag(name = "My Support")
    @Operation(summary = "Get Knowledge Base Article by ID (increments view count)")
    @GetMapping("/knowledge-base/articles/{articleId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<FAQSearchResponse.FAQArticleDto>> getKnowledgeBaseArticleById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("articleId") Long articleId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return (ResponseEntity) unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return (ResponseEntity) forbiddenResponse("support.self.read");
        try {
            return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully",
                    supportService.getKnowledgeBaseArticleById(articleId)));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TAG: My Support - Analytics
    // ═══════════════════════════════════════════════════════════════════════════

    @Tag(name = "My Support")
    @Operation(summary = "Export My Tickets as CSV")
    @GetMapping("/tickets/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<?> exportTickets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");
        try {
            String csv = supportService.exportTicketsCsv(currentUser.getWorkEmail());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename("my-support-tickets.csv").build().toString())
                    .body(csv);
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }
}
