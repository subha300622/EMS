package com.example.ems.employee.controller;
import java.util.List;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Announcement;
import com.example.ems.employee.repository.AnnouncementRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/announcements")
@CrossOrigin("*")
@Tag(name = "System Administration")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<List<Announcement>>> getAnnouncements(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
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

        if (hasManage) {
            return ResponseEntity.ok(ApiResponse.success("Announcements list retrieved successfully",
                    announcementRepository.findAll()));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Announcements list retrieved successfully",
                    announcementRepository.findByActiveTrueOrderByPublishedDateDesc()));
        }
    }

    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAnnouncementById(
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

        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Announcement not found with ID: " + id, "ANC_001"));
        }

        return ResponseEntity.ok(ApiResponse.success("Announcement details retrieved successfully", announcement));
    }

    @PostMapping
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Announcement request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }

        request.setPublishedDate(LocalDateTime.now());
        Announcement saved = announcementRepository.save(request);
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created successfully", saved));
    }

    @PutMapping("/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Announcement request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }

        Announcement announcement = announcementRepository.findById(id).orElse(null);
        if (announcement == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Announcement not found with ID: " + id, "ANC_001"));
        }

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAuthor(request.getAuthor());
        announcement.setActive(request.isActive());

        return ResponseEntity.ok(
                ApiResponse.success("Announcement updated successfully", announcementRepository.save(announcement)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "announcement.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'announcement.manage' permission.", "AUTH_002"));
        }

        if (announcementRepository.existsById(id)) {
            announcementRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Announcement deleted successfully", null));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Announcement not found with ID: " + id, "ANC_001"));
        }
    }

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
}
