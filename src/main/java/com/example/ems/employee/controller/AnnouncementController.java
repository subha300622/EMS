package com.example.ems.employee.controller;

import java.util.Map;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.dto.manager.AnnouncementDto;
import com.example.ems.common.dto.manager.AnnouncementCommentDto;
import com.example.ems.common.service.ManagerNotificationService;
import com.example.ems.employee.entity.Announcement;
import com.example.ems.employee.repository.AnnouncementRepository;
import com.example.ems.employee.repository.AnnouncementCommentRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ems.employee.dto.AddCommentRequest;

@RestController
@RequestMapping("/api/v1/announcements")
@CrossOrigin("*")
@Tag(name = "Notification Management")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementCommentRepository announcementCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ManagerNotificationService managerNotificationService;

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

    private AnnouncementDto mapToAnnouncementDto(Announcement a) {
        int commentCount = announcementCommentRepository.countByAnnouncementId(a.getId());
        return new AnnouncementDto(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getCategory(),
                a.getAuthor(),
                a.getPublishedDate().toLocalDate().toString(),
                a.getLikes(),
                commentCount,
                a.getViews()
        );
    }

    // ── 1. GET ALL ANNOUNCEMENTS (PAGINATED) ──────────────────────────────────
    @Operation(summary = "Get All Announcements", description = "Retrieves a paginated list of announcements depending on roles.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcements list retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AnnouncementDto.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
public ResponseEntity<?> getAnnouncements(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");

        if (!hasManage && !hasRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedDate"));
        Page<Announcement> announcements;
        if (hasManage) {
            announcements = announcementRepository.findAll(pageable);
        } else {
            announcements = announcementRepository.findByActiveTrue(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Announcements list retrieved successfully",
                announcements.map(this::mapToAnnouncementDto)));
    }

    // ── 2. GET ANNOUNCEMENT BY ID ─────────────────────────────────────────────
    @Operation(summary = "Get Announcement Details")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcement details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnouncementDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
public ResponseEntity<?> getAnnouncementById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");

        if (!hasManage && !hasRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }

        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement details retrieved successfully",
                    managerNotificationService.getAnnouncementDetails(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 3. CREATE ANNOUNCEMENT ────────────────────────────────────────────────
    @Operation(summary = "Create Company Announcement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Announcement created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnouncementDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
public ResponseEntity<?> createAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AnnouncementDto requestDto) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created successfully",
                        managerNotificationService.createAnnouncement(currentUser, requestDto)));
    }

    // ── 4. UPDATE ANNOUNCEMENT ────────────────────────────────────────────────
    @Operation(summary = "Update Company Announcement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcement updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnouncementDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PutMapping("/{id}")
public ResponseEntity<?> updateAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody AnnouncementDto requestDto) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement updated successfully",
                    managerNotificationService.updateAnnouncement(id, requestDto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    @Operation(summary = "Delete Company Announcement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcement deleted successfully",
            content = @Content(schema = @Schema(example = "{\"message\": \"Announcement deleted successfully\"}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        try {
            managerNotificationService.deleteAnnouncement(id);
            return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully",
                    Map.of("message", "Announcement deleted successfully")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 6. LIKE ANNOUNCEMENT ──────────────────────────────────────────────────
    @Operation(summary = "Like Company Announcement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcement liked successfully",
            content = @Content(schema = @Schema(example = "{\"likes\": 5}"))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PostMapping("/{id}/like")
public ResponseEntity<?> likeAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement liked successfully",
                    managerNotificationService.likeAnnouncement(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 7. GET COMMENTS ───────────────────────────────────────────────────────
    @Operation(summary = "Get Announcement Comments")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Announcement comments retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AnnouncementCommentDto.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/comments")
public ResponseEntity<?> getComments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Announcement comments retrieved successfully",
                managerNotificationService.getComments(id)));
    }

    // ── 8. ADD COMMENT ────────────────────────────────────────────────────────
    @Operation(summary = "Add Comment to Announcement")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment added successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnnouncementCommentDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PostMapping("/{id}/comments")
public ResponseEntity<?> addComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid AddCommentRequest body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        String content = body != null ? body.content() : null;
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Comment content cannot be empty", "ANC_002"));
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment added successfully",
                            managerNotificationService.addComment(currentUser, id, content)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }
}
