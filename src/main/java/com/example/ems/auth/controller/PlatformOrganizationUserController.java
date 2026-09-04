package com.example.ems.auth.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.dto.OverrideUserRoleRequest;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/platform/organizations/{orgId}/users")
@CrossOrigin("*")
@Tag(name = "Platform Admin - User Role Management")
public class PlatformOrganizationUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

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

    private ResponseEntity<?> organizationSuspendedResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Organization is suspended.", "ORG_001"));
    }

    @GetMapping
    @Operation(summary = "List all users in the organization")
    public ResponseEntity<?> getOrgUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted())
            return organizationSuspendedResponse();

        List<User> users = userRepository.findByOrganizationId(orgId);
        return ResponseEntity.ok(ApiResponse.success("Organization users retrieved successfully.", users));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user details")
    public ResponseEntity<?> getOrgUserDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long userId) {

        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted())
            return organizationSuspendedResponse();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate organization bound
        if (targetUser.getOrganization() == null || !orgId.equals(targetUser.getOrganization().getId())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to specified organization.", "USER_ORG_MISMATCH"));
        }

        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully.", targetUser));
    }

    @GetMapping("/{userId}/permissions")
    @Operation(summary = "Get user effective permissions")
    public ResponseEntity<?> getOrgUserPermissions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long userId) {

        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted())
            return organizationSuspendedResponse();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate organization bound
        if (targetUser.getOrganization() == null || !orgId.equals(targetUser.getOrganization().getId())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to specified organization.", "USER_ORG_MISMATCH"));
        }

        List<String> permissions = roleService.getPermissionsForUserId(targetUser.getUserId());
        return ResponseEntity
                .ok(ApiResponse.success("User effective permissions retrieved successfully.", permissions));
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Override user's role assignment")
    public ResponseEntity<?> overrideUserRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long userId,
            @RequestBody OverrideUserRoleRequest req,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        User user = resolveUser(authHeader);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.",
                            "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted())
            return organizationSuspendedResponse();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate organization bound
        if (targetUser.getOrganization() == null || !orgId.equals(targetUser.getOrganization().getId())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error("User does not belong to specified organization.", "USER_ORG_MISMATCH"));
        }

        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse
                    .error("Reason is required for auditing platform admin overrides.", "AUDIT_REASON_REQUIRED"));
        }

        // Resolve target role
        Optional<Role> targetRoleOpt = Optional.empty();
        if (req.getRoleId() != null) {
            targetRoleOpt = roleRepository.findById(req.getRoleId());
        } else if (req.getRoleName() != null) {
            targetRoleOpt = roleRepository.findByOrganizationIdAndName(orgId, req.getRoleName().trim());
        }

        if (targetRoleOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Specified role not found.", "ROLE_NOT_FOUND"));
        }

        Role targetRole = targetRoleOpt.get();

        // Enforce tenant scoping check
        if (!targetRole.isPlatformTemplate()) {
            Long roleOrgId = targetRole.getOrganization() != null ? targetRole.getOrganization().getId() : null;
            if (roleOrgId == null || !roleOrgId.equals(orgId)) {
                return ResponseEntity.badRequest().body(
                        ErrorResponse.error("Role does not belong to the target organization.", "ROLE_ORG_MISMATCH"));
            }
        }

        targetUser.setRole(targetRole);
        targetUser.setRequestedRole(targetRole.getName());
        User savedUser = userRepository.save(targetUser);

        // Evict caches
        roleService.evictUserPermissionsCache(targetUser.getUserId());

        // Audit Log
        AuditLog auditLog = new AuditLog(
                user.getUserId(),
                user.getWorkEmail(),
                "OVERRIDE_USER_ROLE",
                "User",
                String.valueOf(userId),
                com.example.ems.common.util.ClientIpResolver.getClientIp(servletRequest),
                "Platform Admin overridden user role for user ID: " + userId + " (email: " + targetUser.getWorkEmail()
                        + ") to role '" + targetRole.getName() + "'. Reason: " + req.getReason());
        auditLogRepository.save(auditLog);

        return ResponseEntity.ok(ApiResponse.success("User role overridden successfully.", savedUser));
    }
}
