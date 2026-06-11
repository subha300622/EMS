package com.example.ems.auth.controller;

import com.example.ems.auth.dto.AssignPermissionsRequest;
import com.example.ems.auth.dto.AssignRoleRequest;
import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // ── Create Role ──────────────────────────────────────────────────────────
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        try {
            Role created = roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Role created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    // ── Get Roles ────────────────────────────────────────────────────────────
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Roles list retrieved successfully", roleService.getAllRoles()));
    }

    // ── Update Role ──────────────────────────────────────────────────────────
    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        try {
            return roleService.updateRole(id, request)
                    .<ResponseEntity<?>>map(role -> ResponseEntity.ok(ApiResponse.success("Role updated successfully", role)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Role not found with id " + id, "ROLE_002")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    // ── Patch Role ────────────────────────────────────────────────────────────
    @PatchMapping("/roles/{id}")
    public ResponseEntity<?> patchRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        try {
            return roleService.patchRole(id, updates)
                    .<ResponseEntity<?>>map(role -> ResponseEntity.ok(ApiResponse.success("Role patched successfully", role)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Role not found with id " + id, "ROLE_002")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    // ── Get Role Users ────────────────────────────────────────────────────────
    @GetMapping("/roles/{id}/users")
    public ResponseEntity<?> getRoleUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        if (roleService.getRoleById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with id " + id, "ROLE_002"));
        }

        java.util.List<User> users = userRepository.findByRoleId(id);
        return ResponseEntity.ok(ApiResponse.success("Role users retrieved successfully", users));
    }


    // ── Delete Role ──────────────────────────────────────────────────────────
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        boolean deleted = roleService.deleteRole(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with id " + id, "ROLE_002"));
        }
    }

    // ── Assign Role ──────────────────────────────────────────────────────────
    @PostMapping("/users/{id}/assign-role")
    public ResponseEntity<?> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long userId,
            @RequestBody @Valid AssignRoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        try {
            boolean assigned = roleService.assignRole(userId, request.getRole());
            if (assigned) {
                return ResponseEntity.ok(ApiResponse.success("Role assigned successfully to user"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("User not found with id " + userId, "USR_002"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_004"));
        }
    }

    // ── Assign Permissions to Role ───────────────────────────────────────────
    @PostMapping("/roles/{id}/permissions")
    public ResponseEntity<?> assignPermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long roleId,
            @RequestBody @Valid AssignPermissionsRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' or 'permission.manage' permission.", "AUTH_002"));
        }

        try {
            boolean assigned = roleService.assignPermissionsToRole(roleId, request.getPermissions());
            if (assigned) {
                return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully to role"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Role not found with id " + roleId, "ROLE_002"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_005"));
        }
    }

    // ── Get Role By ID ───────────────────────────────────────────────────────
    @GetMapping("/roles/{id}")
    public ResponseEntity<?> getRoleById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        return roleService.getRoleById(id)
                .<ResponseEntity<?>>map(role -> ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002")));
    }

    // ── Get Permissions for Role ─────────────────────────────────────────────
    @GetMapping("/roles/{id}/permissions")
    public ResponseEntity<?> getRolePermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role management permissions.", "AUTH_002"));
        }

        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Permissions for role retrieved successfully", role.getPermissions()));
    }

    // ── Revoke Permission from Role ──────────────────────────────────────────
    @DeleteMapping("/roles/{id}/permissions/{permissionId}")
    public ResponseEntity<?> revokePermission(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long permissionId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.manage' permission.", "AUTH_002"));
        }

        try {
            boolean revoked = roleService.revokePermissionFromRole(id, permissionId);
            if (revoked) {
                return ResponseEntity.ok(ApiResponse.success("Permission revoked successfully from role"));
            } else {
                return ResponseEntity.badRequest().body(ErrorResponse.error("Role not found or permission was not assigned to this role", "ROLE_006"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_005"));
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
