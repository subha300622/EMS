package com.example.ems.auth.controller;

import com.example.ems.auth.dto.PermissionRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.PermissionService;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@CrossOrigin("*")
@Tag(name = "Permission Master APIs")
public class PermissionMasterController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

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

    private boolean isPlatformAdmin(User user) {
        if (user == null || user.getRole() == null) return false;
        return "PLATFORM_ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    @GetMapping
    @Operation(summary = "List all master permissions", description = "Retrieves master system permissions catalog.")
    public ResponseEntity<?> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissions));
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<?> getPermissionById(@PathVariable Long permissionId) {
        Permission permission = permissionService.getPermissionById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));
        return ResponseEntity.ok(ApiResponse.success("Permission retrieved successfully", permission));
    }

    @PostMapping
    @Operation(summary = "Create master system permission", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> createPermission(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PermissionRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Master permission creation is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        Permission created = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Master permission created successfully", created));
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "Update master permission", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> updatePermission(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long permissionId,
            @Valid @RequestBody PermissionRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Master permission update is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        Permission updated = permissionService.updatePermission(permissionId, request)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));
        roleService.evictAllUserPermissionsCache();
        return ResponseEntity.ok(ApiResponse.success("Master permission updated successfully", updated));
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Soft-deactivate master permission", description = "Deactivates permission without physical deletion. Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> deletePermission(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long permissionId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Master permission deactivation is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        Permission p = permissionService.getPermissionById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

        p.setActive(false);
        Permission updated = permissionService.savePermission(p);
        roleService.evictAllUserPermissionsCache();

        return ResponseEntity.ok(ApiResponse.success("Permission soft-deactivated successfully", updated));
    }

    @PostMapping("/check")
    @Operation(summary = "Check logged-in user permission", description = "Checks whether the caller holds the specified permission.")
    public ResponseEntity<?> checkPermission(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody @jakarta.validation.Valid com.example.ems.auth.dto.CheckPermissionRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        String perm = request != null ? request.permission() : null;
        if (perm == null || perm.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Field 'permission' is required", "VAL_001"));
        }

        boolean allowed = roleService.hasPermission(currentUser.getWorkEmail(), perm);
        return ResponseEntity.ok(ApiResponse.success("Permission check completed", Map.of("permission", perm, "allowed", allowed)));
    }
}
