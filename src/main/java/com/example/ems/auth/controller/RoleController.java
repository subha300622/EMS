package com.example.ems.auth.controller;

import com.example.ems.auth.dto.*;
import java.util.List;
import java.util.stream.Collectors;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Role Management")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // ── Create Role ──────────────────────────────────────────────────────────
    @Operation(summary = "Create Role", description = "Creates a new system security role for RBAC authorization.")
    @PostMapping("/roles")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.create")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.create' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        try {
            Role created = roleService.createRole(request);
            RoleResponse data = new RoleResponse(
                    created.getId(),
                    created.getName(),
                    created.getDescription(),
                    created.getPermissions() != null ? created.getPermissions().size() : 0,
                    created.getCreatedAt() != null ? created.getCreatedAt().toString()
                            : java.time.Instant.now().toString(),
                    null);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Role created successfully", data));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    // ── Get Roles ────────────────────────────────────────────────────────────
    @Operation(summary = "Get All Roles", description = "Retrieves all configured system roles along with metadata.")
    @GetMapping("/roles")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.read' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        List<RoleResponse> data = roleService.getAllRoles().stream()
                .map(r -> new RoleResponse(
                        r.getId(),
                        r.getName(),
                        r.getDescription(),
                        r.getPermissions() != null ? r.getPermissions().size() : 0,
                        null,
                        null))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", data));
    }

    // ── Update Role ──────────────────────────────────────────────────────────
    @Operation(summary = "Update Role", description = "Fully updates the name and description of an existing system role.")
    @PutMapping("/roles/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.update' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        try {
            return (ResponseEntity) roleService.updateRole(id, request)
                    .<ResponseEntity<?>>map(role -> {
                        RoleResponse data = new RoleResponse(role.getId(), role.getName(), role.getDescription(), null,
                                null, null);
                        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", data));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    // ── Patch Role ────────────────────────────────────────────────────────────
    @Operation(summary = "Patch Role Details", description = "Partially updates attributes (such as name or description) of a system role.")
    @PatchMapping("/roles/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> patchRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.update")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.update' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        try {
            return (ResponseEntity) roleService.patchRole(id, updates)
                    .<ResponseEntity<?>>map(role -> {
                        RoleResponse data = new RoleResponse();
                        data.setRoleId(role.getId());
                        if (updates.containsKey("name")) {
                            data.setName(role.getName());
                        }
                        if (updates.containsKey("description")) {
                            data.setDescription(role.getDescription());
                        }
                        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", data));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    // ── Get Role Users ────────────────────────────────────────────────────────
    @Operation(summary = "Get Role Users", description = "Retrieves all users assigned to the specified security role.")
    @GetMapping("/roles/{id}/users")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getRoleUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.read' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        if (roleService.getRoleById(id).isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002"));
        }

        java.util.List<User> users = userRepository.findByRoleId(id);
        List<RoleUserResponse> userList = users.stream()
                .map(u -> new RoleUserResponse(u.getUserId(), u.getFullName(), u.getWorkEmail(), u.getStatus()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Role users retrieved successfully", userList));
    }

    // ── Delete Role ──────────────────────────────────────────────────────────
    @Operation(summary = "Delete Role", description = "Deletes a security role from the system.")
    @DeleteMapping("/roles/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.delete")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.delete' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        boolean deleted = roleService.deleteRole(id);
        if (deleted) {
            DeleteRoleResponse data = new DeleteRoleResponse(id, true);
            return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", data));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002"));
        }
    }


    // ── Assign Permissions to Role ───────────────────────────────────────────
    @Operation(summary = "Assign Permissions to Role", description = "Maps multiple system catalog permissions to a specific role.")
    @PostMapping("/roles/{id}/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> assignPermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("id") Long roleId,
            @RequestBody @Valid AssignPermissionsRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.permission.assign")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "permission.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role/permission management permissions.",
                            "AUTH_002"));
        }

        try {
            boolean assigned;
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                assigned = roleService.assignPermissionIdsToRole(roleId, request.getPermissionIds());
            } else if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
                assigned = roleService.assignPermissionsToRole(roleId, request.getPermissions());
            } else {
                return (ResponseEntity) ResponseEntity.badRequest().body(
                        ErrorResponse.error("Permissions list or Permission IDs list cannot be empty", "ROLE_005"));
            }

            if (assigned) {
                Role role = roleService.getRoleById(roleId).orElseThrow();
                List<String> assignedPerms = role.getPermissions().stream()
                        .map(com.example.ems.auth.entity.Permission::getName)
                        .collect(Collectors.toList());
                AssignPermissionsResponse data = new AssignPermissionsResponse(roleId, assignedPerms);
                return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully", data));
            } else {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Role not found with ID: " + roleId, "ROLE_002"));
            }
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_005"));
        }
    }

    // ── Get Role By ID ───────────────────────────────────────────────────────
    @Operation(summary = "Get Role Details", description = "Retrieves the description, metadata, and permissions mapped to a specific role.")
    @GetMapping("/roles/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getRoleById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'role.read' or 'role.manage' permission.",
                            "AUTH_002"));
        }

        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002"));
        }

        List<PermissionResponse> perms = role.getPermissions().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription()))
                .collect(Collectors.toList());
        RoleResponse data = new RoleResponse(role.getId(), role.getName(), role.getDescription(), null, null, perms);
        return ResponseEntity.ok(ApiResponse.success("Role details retrieved successfully", data));
    }

    // ── Get Permissions for Role ─────────────────────────────────────────────
    @Operation(summary = "Get Role Permissions", description = "Retrieves all permissions currently mapped to the specified role.")
    @GetMapping("/roles/{id}/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getRolePermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.permission.read")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "permission.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role/permission management permissions.",
                            "AUTH_002"));
        }

        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Role not found with ID: " + id, "ROLE_002"));
        }

        List<PermissionResponse> perms = role.getPermissions().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully", perms));
    }

    // ── Revoke Permission from Role ──────────────────────────────────────────
    @Operation(summary = "Revoke Permission from Role", description = "Removes a specific access permission mapping from a security role.")
    @DeleteMapping("/roles/{id}/permissions/{permissionId}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> revokePermission(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long permissionId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "role.permission.assign")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "permission.manage")
                && !roleService.hasPermission(currentUser.getWorkEmail(), "role.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role/permission management permissions.",
                            "AUTH_002"));
        }

        try {
            boolean revoked = roleService.revokePermissionFromRole(id, permissionId);
            if (revoked) {
                RemovePermissionResponse data = new RemovePermissionResponse(id, permissionId);
                return ResponseEntity.ok(ApiResponse.success("Permission removed successfully", data));
            } else {
                return (ResponseEntity) ResponseEntity.badRequest().body(
                        ErrorResponse.error("Role not found or permission was not assigned to this role", "ROLE_006"));
            }
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_005"));
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
