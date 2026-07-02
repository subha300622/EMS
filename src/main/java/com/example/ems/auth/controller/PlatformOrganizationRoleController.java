package com.example.ems.auth.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.dto.*;
import com.example.ems.auth.entity.Permission;
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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/platform/organizations/{orgId}/roles")
@CrossOrigin("*")
@Tag(name = "Platform Admin - Organization Role Overrides")
public class PlatformOrganizationRoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

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
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission) || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> organizationSuspendedResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Organization is suspended.", "ORG_001"));
    }

    private void validateOrgRole(Long roleId, Long orgId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        Long roleOrgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
        if (roleOrgId == null || !roleOrgId.equals(orgId)) {
            throw new IllegalArgumentException("Role does not belong to specified organization.");
        }
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all organization roles with customized filter")
    public ResponseEntity<?> getOrgRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @RequestParam(required = false) Boolean customized) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        List<Role> roles = roleRepository.findByOrganizationId(orgId);

        if (customized != null) {
            roles = roles.stream().filter(r -> {
                Optional<Role> templateOpt = roleRepository.findByNameAndIsPlatformTemplateTrue(r.getName());
                if (templateOpt.isPresent()) {
                    Set<String> rolePerms = r.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
                    Set<String> templatePerms = templateOpt.get().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
                    boolean matches = rolePerms.equals(templatePerms);
                    return customized ? !matches : matches;
                }
                return customized; // Custom roles without templates count as customized
            }).collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Organization roles retrieved successfully.", roles));
    }

    @PostMapping
    @Operation(summary = "Create custom role for organization")
    public ResponseEntity<?> createOrgRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @Valid @RequestBody RoleRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        try {
            Role role = roleService.createTenantRole(orgId, req);

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_CREATE_ROLE",
                    "Role",
                    String.valueOf(role.getId()),
                    "127.0.0.1",
                    "Platform Admin created tenant role '" + role.getName() + "' for organization ID: " + orgId
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Organization role created successfully.", role));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_CREATE_ERR"));
        }
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get organization role details")
    public ResponseEntity<?> getOrgRoleDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        Role role = roleRepository.findById(roleId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Role details retrieved successfully.", role));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update organization role details")
    public ResponseEntity<?> updateOrgRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        try {
            Role updated = roleService.updateTenantRole(roleId, orgId, req);

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_UPDATE_ROLE",
                    "Role",
                    String.valueOf(roleId),
                    "127.0.0.1",
                    "Platform Admin updated tenant role details for role ID: " + roleId
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.ok(ApiResponse.success("Role updated successfully.", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_UPDATE_ERR"));
        }
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete organization role")
    public ResponseEntity<?> deleteOrgRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        try {
            roleService.deleteTenantRole(roleId, orgId);

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_DELETE_ROLE",
                    "Role",
                    String.valueOf(roleId),
                    "127.0.0.1",
                    "Platform Admin deleted tenant role ID: " + roleId
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.ok(ApiResponse.success("Role deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_DELETE_ERR"));
        }
    }

    @GetMapping("/{roleId}/users")
    @Operation(summary = "Get list of users assigned to organization role")
    public ResponseEntity<?> getOrgRoleUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        List<User> assignedUsers = roleService.getRoleUsers(roleId);

        List<Map<String, Object>> userItems = assignedUsers.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", u.getId());
            map.put("name", u.getFullName());
            map.put("email", u.getWorkEmail());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Users assigned to role retrieved successfully.", userItems));
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Get role permissions")
    public ResponseEntity<?> getOrgRolePermissions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        Role role = roleRepository.findById(roleId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success("Role permissions retrieved successfully.", role.getPermissions()));
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Override organization role permissions with auditing reason")
    public ResponseEntity<?> overrideOrgRolePermissions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId,
            @RequestBody OverrideRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.permission.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.permission.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Reason is required for auditing platform admin overrides.", "AUDIT_REASON_REQUIRED"));
        }

        try {
            roleService.assignPermissionIdsToRole(roleId, req.getPermissionIds());

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_ROLE_PERMISSIONS",
                    "Role",
                    String.valueOf(roleId),
                    "127.0.0.1",
                    "Platform Admin overrode role permissions for role ID: " + roleId + ". Reason: " + req.getReason()
            );
            auditLogRepository.save(auditLog);

            Role updatedRole = roleRepository.findById(roleId).orElseThrow();
            return ResponseEntity.ok(ApiResponse.success("Role permissions overridden successfully.", updatedRole));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_PERM_OVERRIDE_ERR"));
        }
    }

    @GetMapping("/{roleId}/compare-template")
    @Operation(summary = "Compare organization role permissions with template")
    public ResponseEntity<?> compareOrgRoleToTemplate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        try {
            TemplateDiffResponse diff = roleService.compareRoleToTemplate(roleId);
            Role role = roleRepository.findById(roleId).orElseThrow();
            boolean isCustomized = !diff.getAdded().isEmpty() || !diff.getRemoved().isEmpty();

            TemplateComparisonResponse response = new TemplateComparisonResponse(
                    role.getName(),
                    isCustomized,
                    diff.getAdded(),
                    diff.getRemoved()
            );

            return ResponseEntity.ok(ApiResponse.success("Comparison completed successfully.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "COMPARE_ERR"));
        }
    }

    @GetMapping("/{roleId}/template-diff")
    @Operation(summary = "Get preview details comparing role to template")
    public ResponseEntity<?> getTemplateDiffPreview(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        try {
            TemplateDiffResponse diff = roleService.compareRoleToTemplate(roleId);
            return ResponseEntity.ok(ApiResponse.success("Template diff preview retrieved successfully.", diff));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "DIFF_PREVIEW_ERR"));
        }
    }

    @PostMapping("/{roleId}/sync-template")
    @Operation(summary = "Synchronize organization role to template baseline")
    public ResponseEntity<?> syncOrgRoleWithTemplate(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @PathVariable Long roleId,
            @RequestBody Map<String, String> body) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        validateOrgRole(roleId, orgId);

        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Reason is required for auditing platform admin overrides.", "AUDIT_REASON_REQUIRED"));
        }

        try {
            roleService.syncRoleWithTemplate(roleId);

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_SYNC_ROLE",
                    "Role",
                    String.valueOf(roleId),
                    "127.0.0.1",
                    "Platform Admin synchronized role ID: " + roleId + " with platform template. Reason: " + reason
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.ok(ApiResponse.success("Role synchronized with platform template successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "SYNC_ERR"));
        }
    }

    @GetMapping("/role-stats")
    @Operation(summary = "Get user and permission usage metrics per role")
    public ResponseEntity<?> getOrgRoleStats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        List<RoleStatsResponse> stats = roleService.getRoleStats(orgId);
        return ResponseEntity.ok(ApiResponse.success("Role statistics retrieved successfully.", stats));
    }

    @GetMapping("/permission-stats")
    @Operation(summary = "Get usage count statistics per permission")
    public ResponseEntity<?> getOrgPermissionStats(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        Map<String, Long> stats = roleService.getPermissionStats(orgId);
        return ResponseEntity.ok(ApiResponse.success("Permission statistics retrieved successfully.", stats));
    }

    @GetMapping("/customizations")
    @Operation(summary = "List only customized organization roles")
    public ResponseEntity<?> getOrgCustomizations(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        List<RoleStatsResponse> customizations = roleService.getCustomizations(orgId);
        return ResponseEntity.ok(ApiResponse.success("Customized roles retrieved successfully.", customizations));
    }

    @PostMapping("/bulk-delete")
    @Operation(summary = "Bulk delete organization roles")
    public ResponseEntity<?> bulkDeleteRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @RequestBody BulkDeleteRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Reason is required for auditing platform admin overrides.", "AUDIT_REASON_REQUIRED"));
        }

        try {
            for (Long roleId : req.getRoleIds()) {
                validateOrgRole(roleId, orgId);
                roleService.deleteTenantRole(roleId, orgId);
            }

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_BULK_DELETE_ROLES",
                    "Role",
                    req.getRoleIds().toString(),
                    "127.0.0.1",
                    "Platform Admin bulk deleted roles: " + req.getRoleIds() + ". Reason: " + req.getReason()
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.ok(ApiResponse.success("Roles bulk deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "BULK_DELETE_ERR"));
        }
    }

    @PostMapping("/bulk-sync")
    @Operation(summary = "Bulk synchronize organization roles to template baseline")
    public ResponseEntity<?> bulkSyncRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId,
            @RequestBody BulkSyncRequest req) {

        User user = resolveUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!checkPermission(user, "platform.role.override")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.role.override' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) return organizationSuspendedResponse();

        if (req.getReason() == null || req.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Reason is required for auditing platform admin overrides.", "AUDIT_REASON_REQUIRED"));
        }

        try {
            for (Long roleId : req.getRoleIds()) {
                validateOrgRole(roleId, orgId);
                roleService.syncRoleWithTemplate(roleId);
            }

            // Audit
            AuditLog auditLog = new AuditLog(
                    user.getUserId(),
                    user.getWorkEmail(),
                    "OVERRIDE_BULK_SYNC_ROLES",
                    "Role",
                    req.getRoleIds().toString(),
                    "127.0.0.1",
                    "Platform Admin bulk synchronized roles: " + req.getRoleIds() + " with templates. Reason: " + req.getReason()
            );
            auditLogRepository.save(auditLog);

            return ResponseEntity.ok(ApiResponse.success("Roles bulk synchronized successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "BULK_SYNC_ERR"));
        }
    }
}
