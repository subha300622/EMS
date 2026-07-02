package com.example.ems.auth.controller;

import com.example.ems.auth.dto.RoleRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizations/roles")
@CrossOrigin("*")
@Tag(name = "Organization Role Administration")
public class OrganizationRoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveOrgAdmin(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email)
                        .filter(u -> roleService.hasPermission(email, "role.manage") || roleService.isSuperAdmin(email))
                        .orElse(null);
            }
        }
        return null;
    }

    @Operation(summary = "List Organization Roles")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listRoles(@RequestHeader("Authorization") String authHeader) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }
        List<Role> roles = roleService.getTenantRoles(orgId);
        return ResponseEntity.ok(ApiResponse.success("Organization roles retrieved successfully", roles));
    }

    @Operation(summary = "Create Custom Organization Role")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createRole(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody @Valid RoleRequest request) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }
        try {
            Role created = roleService.createTenantRole(orgId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Organization role created successfully", created));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    @Operation(summary = "Get Custom Organization Role Details")
    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getRole(@RequestHeader("Authorization") String authHeader,
                                                       @PathVariable Long id) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null || (role.getOrganization() != null && !role.getOrganization().getId().equals(orgId))) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Organization role not found with ID: " + id, "ROLE_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Organization role retrieved successfully", role));
    }

    @Operation(summary = "Update Custom Organization Role")
    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateRole(@RequestHeader("Authorization") String authHeader,
                                                          @PathVariable Long id,
                                                          @RequestBody @Valid RoleRequest request) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }
        try {
            Role updated = roleService.updateTenantRole(id, orgId, request);
            return ResponseEntity.ok(ApiResponse.success("Organization role updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    @Operation(summary = "Delete Custom Organization Role")
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteRole(@RequestHeader("Authorization") String authHeader,
                                                          @PathVariable Long id) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        if (orgId == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to any organization", "AUTH_017"));
        }
        try {
            roleService.deleteTenantRole(id, orgId);
            return ResponseEntity.ok(ApiResponse.success("Organization role deleted successfully"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    @Operation(summary = "Get Organization Role Permissions")
    @GetMapping("/{id}/permissions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getRolePermissions(@RequestHeader("Authorization") String authHeader,
                                                                  @PathVariable Long id) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null || (role.getOrganization() != null && !role.getOrganization().getId().equals(orgId))) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Organization role not found with ID: " + id, "ROLE_002"));
        }
        List<Map<String, Object>> perms = role.getPermissions().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName(), "description", p.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Organization role permissions retrieved successfully", perms));
    }

    @Operation(summary = "Update Organization Role Permissions")
    @PutMapping("/{id}/permissions")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateRolePermissions(@RequestHeader("Authorization") String authHeader,
                                                                     @PathVariable Long id,
                                                                     @RequestBody Map<String, List<Long>> request) {
        User admin = resolveOrgAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires role.manage permission", "AUTH_002"));
        }
        Long orgId = admin.getOrganization() != null ? admin.getOrganization().getId() : null;
        Role role = roleService.getRoleById(id).orElse(null);
        if (role == null || (role.getOrganization() != null && !role.getOrganization().getId().equals(orgId))) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Organization role not found with ID: " + id, "ROLE_002"));
        }
        List<Long> permissionIds = request.get("permissionIds");
        if (permissionIds == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Request body must contain 'permissionIds' array.", "ROLE_004"));
        }
        try {
            boolean success = roleService.assignPermissionIdsToRole(id, permissionIds);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Organization role permissions updated successfully"));
            } else {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Role not found", "ROLE_002"));
            }
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }
}
