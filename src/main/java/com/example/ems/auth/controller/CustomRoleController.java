package com.example.ems.auth.controller;

import com.example.ems.auth.dto.RoleRequest;
import com.example.ems.auth.dto.RoleResponse;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@CrossOrigin("*")
@Tag(name = "Custom Role APIs")
public class CustomRoleController {

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

    private boolean checkPermission(User user) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), "role.manage")
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    @GetMapping
    @Operation(summary = "List organization custom roles", description = "Lists custom roles for the caller's organization context.")
    public ResponseEntity<?> getOrganizationRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<Role> roles = roleService.getTenantRoles();
        List<RoleResponse> responseList = roles.stream()
                .map(roleService::mapRoleToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Organization custom roles retrieved successfully", responseList));
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get custom role by ID")
    public ResponseEntity<?> getRoleById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", roleService.mapRoleToResponse(role)));
    }

    @PostMapping
    @Operation(summary = "Create custom role", description = "Creates a new custom role scoped to caller's organization.")
    public ResponseEntity<?> createCustomRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Role created = roleService.createTenantRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom role created successfully", roleService.mapRoleToResponse(created)));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update custom role")
    public ResponseEntity<?> updateCustomRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Role updated = roleService.updateTenantRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Custom role updated successfully", roleService.mapRoleToResponse(updated)));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete custom role")
    public ResponseEntity<?> deleteCustomRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        roleService.deleteTenantRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Custom role deleted successfully"));
    }

    // ── Permissions Assignment to Role ──────────────────────────────────────────

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Get role permissions")
    public ResponseEntity<?> getRolePermissions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully", role.getPermissions()));
    }

    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permissions to role")
    public ResponseEntity<?> assignPermissionsToRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @RequestBody @Valid com.example.ems.auth.dto.AssignPermissionsRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        List<Long> permissionIds = request.getPermissionIds();
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleService.assignPermissionIdsToRole(roleId, permissionIds);
        } else {
            List<String> permissionNames = request.getPermissionNames() != null ? request.getPermissionNames() : request.getPermissions();
            if (permissionNames != null && !permissionNames.isEmpty()) {
                roleService.assignPermissionsToRole(roleId, permissionNames);
            }
        }

        Role updated = roleService.getRoleById(roleId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned to role successfully", roleService.mapRoleToResponse(updated)));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from role")
    public ResponseEntity<?> removePermissionFromRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        roleService.revokePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully"));
    }

    // ── Permission Group Assignment to Role ──────────────────────────────────────

    @GetMapping("/{roleId}/permission-groups")
    @Operation(summary = "Get role permission groups")
    public ResponseEntity<?> getRolePermissionGroups(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);

        return ResponseEntity.ok(ApiResponse.success("Role permission groups retrieved successfully", role.getPermissionGroups()));
    }

    @PostMapping("/{roleId}/permission-groups")
    @Operation(summary = "Assign permission groups to role")
    public ResponseEntity<?> assignPermissionGroupsToRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @RequestBody @Valid com.example.ems.auth.dto.AssignPermissionGroupsRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);

        List<Long> groupIds = request.permissionGroupIds();
        if (groupIds != null) {
            RoleRequest req = new RoleRequest();
            req.setName(role.getName());
            req.setDescription(role.getDescription());
            req.setPermissionGroupIds(groupIds);
            roleService.updateTenantRole(roleId, req);
        }

        Role updated = roleService.getRoleById(roleId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Permission groups assigned to role successfully", roleService.mapRoleToResponse(updated)));
    }

    @DeleteMapping("/{roleId}/permission-groups/{groupId}")
    @Operation(summary = "Remove permission group from role")
    public ResponseEntity<?> removePermissionGroupFromRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @PathVariable Long groupId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);

        roleService.removePermissionGroupFromRole(role.getId(), groupId);
        return ResponseEntity.ok(ApiResponse.success("Permission group removed from role successfully"));
    }

    // ── User Role Assignment ───────────────────────────────────────────────────

    @PostMapping("/{roleId}/users")
    @Operation(summary = "Assign role to employee/user", description = "Assigns the specified role to an employee within caller's organization.")
    public ResponseEntity<?> assignRoleToUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long roleId,
            @RequestBody @Valid com.example.ems.auth.dto.AssignRoleToUserRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }

        Role role = roleService.requireRoleOwnedByCurrentTenant(roleId);
        Long orgId = roleService.currentOrganizationId();

        String rawUserVal = request != null ? request.getEffectiveUserId() : null;
        if (rawUserVal == null || rawUserVal.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Field 'userId' is required", "VAL_001"));
        }

        boolean assigned = false;
        try {
            Long uId = Long.parseLong(rawUserVal);
            assigned = roleService.assignRoleById(uId, role.getId(), orgId);
        } catch (NumberFormatException e) {
            User targetUser = userRepository.findByUserId(rawUserVal)
                    .or(() -> userRepository.findByWorkEmail(rawUserVal))
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + rawUserVal));
            assigned = roleService.assignRoleById(targetUser.getId(), role.getId(), orgId);
        }

        if (!assigned) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Could not assign role to user", "VAL_003"));
        }

        return ResponseEntity.ok(ApiResponse.success("Role assigned to user successfully"));
    }
}
