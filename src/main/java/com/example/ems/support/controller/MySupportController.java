package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.service.MySupportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support")
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
        if (roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail())) {
            return true;
        }
        // Fallback mappings to keep backward compatibility or roles alignment
        if ("support.self.read".equals(permission)) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.read");
        }
        if ("support.self.create".equals(permission)) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.create");
        }
        if ("support.self.comment.create".equals(permission)) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.update");
        }
        if ("support.self.close".equals(permission)) {
            return roleService.hasPermission(user.getWorkEmail(), "employee.support-ticket.update");
        }
        return false;
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // 1. Get Support Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            SupportDashboardResponse response = supportService.getDashboard(currentUser.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 2. Create Support Ticket
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody CreateTicketRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return forbiddenResponse("support.self.create");

        try {
            CreateTicketResponse response = supportService.createTicket(currentUser.getWorkEmail(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Support ticket created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 3. Get My Tickets (paginated/filtered)
    @GetMapping("/tickets")
    public ResponseEntity<?> getMyTickets(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            MyTicketsResponse response = supportService.getMyTickets(currentUser.getWorkEmail(), status, priority, categoryId, search, pageable);
            return ResponseEntity.ok(ApiResponse.success("Tickets list retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 4. Get Ticket Details
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            TicketDetailsResponse response = supportService.getTicketDetails(currentUser.getWorkEmail(), ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 5. Add Comment
    @PostMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<?> addComment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody AddCommentRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.comment.create")) return forbiddenResponse("support.self.comment.create");

        try {
            AddCommentResponse response = supportService.addComment(currentUser.getWorkEmail(), ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Comment added successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 6. Escalate Ticket
    @PatchMapping("/tickets/{ticketId}/escalate")
    public ResponseEntity<?> escalateTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody EscalateTicketRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return forbiddenResponse("support.self.create");

        try {
            EscalateTicketResponse response = supportService.escalateTicket(currentUser.getWorkEmail(), ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Ticket escalated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 7. Close Ticket
    @PatchMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId,
            @Valid @RequestBody CloseTicketRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.close")) return forbiddenResponse("support.self.close");

        try {
            CloseTicketResponse response = supportService.closeTicket(currentUser.getWorkEmail(), ticketId, request);
            return ResponseEntity.ok(ApiResponse.success("Ticket closed successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 8. Get Support Categories
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            List<SupportCategoryResponse> response = supportService.getCategories();
            return ResponseEntity.ok(ApiResponse.success("Support categories retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 9. Upload Attachment
    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam("file") MultipartFile file) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.create")) return forbiddenResponse("support.self.create");

        try {
            AttachmentUploadResponse response = supportService.uploadAttachment(file);
            return ResponseEntity.ok(ApiResponse.success("Attachment uploaded successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 10. Get Ticket Timeline
    @GetMapping("/tickets/{ticketId}/timeline")
    public ResponseEntity<?> getTicketTimeline(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("ticketId") Long ticketId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            TicketTimelineResponse response = supportService.getTicketTimeline(currentUser.getWorkEmail(), ticketId);
            return ResponseEntity.ok(ApiResponse.success("Ticket timeline retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }

    // 11. FAQ Knowledge Base search
    @GetMapping("/knowledge-base")
    public ResponseEntity<?> searchFAQ(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "keyword", required = false) String keyword) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) return unauthorizedResponse();
        if (!checkPermission(currentUser, "support.self.read")) return forbiddenResponse("support.self.read");

        try {
            FAQSearchResponse response = supportService.searchFAQ(keyword);
            return ResponseEntity.ok(ApiResponse.success("Knowledge base articles retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(e.getMessage(), "SUP_500"));
        }
    }
}
