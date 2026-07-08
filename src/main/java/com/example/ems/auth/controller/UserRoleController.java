package com.example.ems.auth.controller;

import com.example.ems.auth.dto.AssignRoleRequest;
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

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
@Tag(name = "User Role Assignment")
public class UserRoleController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

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

    private boolean checkPermission(User user, String permission) {
        if (user == null)
            return false;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    @GetMapping("/{id}/role")
    @Operation(summary = "Get user role details")
    public ResponseEntity<?> getUserRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Tenant isolation
        Long currentOrgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null;
        Long targetOrgId = targetUser.getOrganization() != null ? targetUser.getOrganization().getId() : null;
        if (!roleService.isSuperAdmin(currentUser.getWorkEmail()) &&
                (currentOrgId == null || !currentOrgId.equals(targetOrgId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        Role role = targetUser.getRole();
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User does not have an assigned role.", "ROLE_NOT_ASSIGNED"));
        }

        return ResponseEntity.ok(ApiResponse.success("User role retrieved successfully.", role));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Assign role to a user")
    public ResponseEntity<?> assignUserRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest req) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(currentUser, "user.role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires user.role.assign permission", "AUTH_002"));
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Tenant isolation
        Long currentOrgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null;
        Long targetOrgId = targetUser.getOrganization() != null ? targetUser.getOrganization().getId() : null;
        if (!roleService.isSuperAdmin(currentUser.getWorkEmail()) &&
                (currentOrgId == null || !currentOrgId.equals(targetOrgId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        try {
            boolean success = false;
            if (req.getRoleId() != null) {
                success = roleService.assignRoleById(targetUser.getId(), req.getRoleId());
            } else if (req.getRole() != null) {
                success = roleService.assignRole(targetUser.getId(), req.getRole());
            }

            if (success) {
                return ResponseEntity.ok(ApiResponse.success("User role assigned successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.error("Failed to assign role.", "ROLE_ASSIGN_FAIL"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_ASSIGN_ERR"));
        }
    }

    @GetMapping("/{id}/effective-permissions")
    @Operation(summary = "Resolve effective permissions for a user")
    public ResponseEntity<?> getEffectivePermissions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Tenant isolation
        Long currentOrgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : null;
        Long targetOrgId = targetUser.getOrganization() != null ? targetUser.getOrganization().getId() : null;
        if (!roleService.isSuperAdmin(currentUser.getWorkEmail()) &&
                (currentOrgId == null || !currentOrgId.equals(targetOrgId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        List<String> permissions = roleService.getPermissionsForUserId(targetUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("User effective permissions resolved successfully.", permissions));
    }
}
