package com.example.ems.common.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.dto.manager.*;
import com.example.ems.common.service.ManagerNotificationService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin("*")
@Tag(name = "Notification Management")
public class NotificationController {

    @Autowired
    private ManagerNotificationService managerNotificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

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

    // ── 1. GET PAGINATED NOTIFICATION FEED ────────────────────────────────────
    @Operation(summary = "Get Notification Feed")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotificationFeed(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "type", defaultValue = "ALL") String type,
            @RequestParam(name = "status", defaultValue = "ALL") String status) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Notification feed retrieved successfully",
                managerNotificationService.getNotificationFeed(currentUser, page, size, type, status)));
    }


    // ── 3. GET UNREAD NOTIFICATIONS COUNT ─────────────────────────────────────
    @Operation(summary = "Get Unread Notifications Count")
    @GetMapping("/unread-count")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<UnreadCountDto>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully",
                managerNotificationService.getUnreadCount(currentUser)));
    }

    // ── 4. MARK SPECIFIC NOTIFICATION AS READ ─────────────────────────────────
    @Operation(summary = "Mark Specific Notification as Read")
    @PutMapping("/{id}/read")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            managerNotificationService.markAsRead(currentUser, id);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "NOTIF_001"));
        }
    }

    // ── 5. MARK ALL NOTIFICATIONS AS READ ─────────────────────────────────────
    @Operation(summary = "Mark All Notifications as Read")
    @PutMapping("/read-all")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        managerNotificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    // ── 6. DELETE SPECIFIC NOTIFICATION ──────────────────────────────────────
    @Operation(summary = "Delete Specific Notification")
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        try {
            managerNotificationService.deleteNotification(currentUser, id);
            return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.error(e.getMessage(), "NOTIF_002"));
        }
    }

    // ── 7. GET NOTIFICATION PREFERENCES ───────────────────────────────────────
    @Operation(summary = "Get Notification Preferences")
    @GetMapping("/preferences")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> getPreferences(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Notification preferences retrieved successfully",
                managerNotificationService.getPreferences(currentUser)));
    }

    // ── 8. UPDATE NOTIFICATION PREFERENCES ───────────────────────────────────────
    @Operation(summary = "Update Notification Preferences")
    @PutMapping("/preferences")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> updatePreferences(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody NotificationPreferenceDto requestDto) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated successfully",
                managerNotificationService.updatePreferences(currentUser, requestDto)));
    }

    // ── 9. GET NOTIFICATION STATS ─────────────────────────────────────────────
    @Operation(summary = "Get Notification Stats")
    @GetMapping("/stats")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<NotificationStatsDto>> getStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Notification stats retrieved successfully",
                managerNotificationService.getStats(currentUser)));
    }

    // ── 10. GET CONSOLIDATED PAGE LOAD DATA ───────────────────────────────────
    @Operation(summary = "Get Consolidated Page Load Data")
    @GetMapping("/page-data")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getPageData(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        return ResponseEntity.ok(ApiResponse.success("Page-load data retrieved successfully",
                managerNotificationService.getPageData(currentUser)));
    }
}
