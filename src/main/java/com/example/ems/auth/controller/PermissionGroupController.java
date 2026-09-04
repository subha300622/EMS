package com.example.ems.auth.controller;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.PermissionGroup;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionGroupRepository;
import com.example.ems.auth.repository.PermissionRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/v1/permission-groups")
@CrossOrigin("*")
@Tag(name = "Permission Group APIs")
public class PermissionGroupController {

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.ems.auth.service.RoleService roleService;

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
    @Operation(summary = "List permission groups")
    public ResponseEntity<?> getAllPermissionGroups() {
        List<PermissionGroup> groups = permissionGroupRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Permission groups retrieved successfully", groups));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get permission group by ID")
    public ResponseEntity<?> getPermissionGroupById(@PathVariable Long groupId) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Permission group not found with ID: " + groupId));
        return ResponseEntity.ok(ApiResponse.success("Permission group retrieved successfully", group));
    }

    @PostMapping
    @Operation(summary = "Create permission group", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> createPermissionGroup(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody @jakarta.validation.Valid com.example.ems.auth.dto.CreatePermissionGroupRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Permission group creation is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        String code = request != null ? request.code() : null;
        String name = request != null ? request.name() : null;
        String description = request != null ? request.description() : null;

        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Fields 'code' and 'name' are required", "VAL_001"));
        }

        if (permissionGroupRepository.findByCode(code.trim()).isPresent()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Permission group with code '" + code + "' already exists", "VAL_002"));
        }

        PermissionGroup group = new PermissionGroup();
        group.setCode(code.trim());
        group.setName(name.trim());
        group.setDescription(description);

        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>();
            for (Long pId : request.permissionIds()) {
                Permission p = permissionRepository.findById(pId)
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + pId));
                perms.add(p);
            }
            group.setPermissions(perms);
        }

        PermissionGroup created = permissionGroupRepository.save(group);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission group created successfully", created));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update permission group", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> updatePermissionGroup(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long groupId,
            @RequestBody @jakarta.validation.Valid com.example.ems.auth.dto.UpdatePermissionGroupRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Permission group update is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Permission group not found with ID: " + groupId));

        if (request != null && request.name() != null && !request.name().isBlank()) {
            group.setName(request.name().trim());
        }
        if (request != null && request.description() != null) {
            group.setDescription(request.description());
        }

        PermissionGroup updated = permissionGroupRepository.save(group);
        roleService.evictAllUserPermissionsCache();
        return ResponseEntity.ok(ApiResponse.success("Permission group updated successfully", updated));
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete permission group", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> deletePermissionGroup(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long groupId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Permission group deletion is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        if (!permissionGroupRepository.existsById(groupId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Permission group not found", "RES_404"));
        }

        permissionGroupRepository.deleteById(groupId);
        roleService.evictAllUserPermissionsCache();
        return ResponseEntity.ok(ApiResponse.success("Permission group deleted successfully"));
    }

    @GetMapping("/{groupId}/permissions")
    @Operation(summary = "List group's permissions")
    public ResponseEntity<?> getGroupPermissions(@PathVariable Long groupId) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Permission group not found with ID: " + groupId));
        return ResponseEntity.ok(ApiResponse.success("Group permissions retrieved successfully", group.getPermissions()));
    }

    @PostMapping("/{groupId}/permissions")
    @Operation(summary = "Add permissions to group", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> addPermissionsToGroup(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long groupId,
            @RequestBody @jakarta.validation.Valid com.example.ems.auth.dto.AddPermissionsToGroupRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Group permissions modification is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Permission group not found with ID: " + groupId));

        List<Long> permissionIds = request != null ? request.permissionIds() : null;
        if (permissionIds != null) {
            Set<Permission> currentPerms = group.getPermissions();
            for (Long pId : permissionIds) {
                Permission p = permissionRepository.findById(pId)
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + pId));
                currentPerms.add(p);
            }
            group.setPermissions(currentPerms);
            permissionGroupRepository.save(group);
        }
        roleService.evictAllUserPermissionsCache();

        return ResponseEntity.ok(ApiResponse.success("Permissions added to group successfully", group.getPermissions()));
    }

    @DeleteMapping("/{groupId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from group", description = "Restricted to PLATFORM_ADMIN.")
    public ResponseEntity<?> removePermissionFromGroup(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long groupId,
            @PathVariable Long permissionId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!isPlatformAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Group permissions modification is restricted to PLATFORM_ADMIN", "AUTH_002"));
        }

        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Permission group not found with ID: " + groupId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

        group.getPermissions().remove(permission);
        permissionGroupRepository.save(group);
        roleService.evictAllUserPermissionsCache();

        return ResponseEntity.ok(ApiResponse.success("Permission removed from group successfully"));
    }
}
