package com.example.ems.employee.controller;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Page<AnnouncementDto>>> getAnnouncements(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");

        if (!hasManage && !hasRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
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
    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AnnouncementDto>> getAnnouncementById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");

        if (!hasManage && !hasRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }

        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement details retrieved successfully",
                    managerNotificationService.getAnnouncementDetails(id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 3. CREATE ANNOUNCEMENT ────────────────────────────────────────────────
    @Operation(summary = "Create Company Announcement")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AnnouncementDto>> createAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AnnouncementDto requestDto) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created successfully",
                        managerNotificationService.createAnnouncement(currentUser, requestDto)));
    }

    // ── 4. UPDATE ANNOUNCEMENT ────────────────────────────────────────────────
    @Operation(summary = "Update Company Announcement")
    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AnnouncementDto>> updateAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody AnnouncementDto requestDto) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement updated successfully",
                    managerNotificationService.updateAnnouncement(id, requestDto)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 5. DELETE ANNOUNCEMENT ────────────────────────────────────────────────
    @Operation(summary = "Delete Company Announcement")
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }
        try {
            managerNotificationService.deleteAnnouncement(id);
            return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully",
                    Map.of("message", "Announcement deleted successfully")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 6. LIKE ANNOUNCEMENT ──────────────────────────────────────────────────
    @Operation(summary = "Like Company Announcement")
    @PostMapping("/{id}/like")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Integer>>> likeAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success("Announcement liked successfully",
                    managerNotificationService.likeAnnouncement(id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }

    // ── 7. GET COMMENTS ───────────────────────────────────────────────────────
    @Operation(summary = "Get Announcement Comments")
    @GetMapping("/{id}/comments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<AnnouncementCommentDto>>> getComments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Announcement comments retrieved successfully",
                managerNotificationService.getComments(id)));
    }

    // ── 8. ADD COMMENT ────────────────────────────────────────────────────────
    @Operation(summary = "Add Comment to Announcement")
    @PostMapping("/{id}/comments")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<AnnouncementCommentDto>> addComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        boolean hasManage = roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage");
        boolean hasRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.announcement.read");
        if (!hasManage && !hasRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires announcement read permissions.", "AUTH_002"));
        }
        String content = body != null ? body.get("content") : null;
        if (content == null || content.trim().isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error("Comment content cannot be empty", "ANC_002"));
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Comment added successfully",
                            managerNotificationService.addComment(currentUser, id, content)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ANC_001"));
        }
    }
}
