package com.example.ems.auth.controller;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.dto.OrganizationRbacSummary;
import com.example.ems.auth.dto.PlatformOrganizationSummaryResponse;
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
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/platform/organizations")
@CrossOrigin("*")
@Tag(name = "Platform Admin - Organization Management")
public class PlatformOrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List Organizations with pagination & filtering")
    public ResponseEntity<?> getOrganizations(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.organization.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.organization.view' permission.", "AUTH_002"));
        }

        try {
            List<Organization> allOrgs = organizationRepository.findAll();

            // Filter
            List<Organization> filtered = allOrgs.stream()
                    .filter(org -> {
                        if (status != null && !status.isEmpty()) {
                            boolean active = !org.isDeleted();
                            if ("ACTIVE".equalsIgnoreCase(status) && !active) return false;
                            if ("SUSPENDED".equalsIgnoreCase(status) && active) return false;
                        }
                        if (search != null && !search.isEmpty()) {
                            String query = search.toLowerCase();
                            boolean matchName = org.getName().toLowerCase().contains(query);
                            boolean matchCode = org.getOrganizationCode().toLowerCase().contains(query);
                            return matchName || matchCode;
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(Organization::getId))
                    .collect(Collectors.toList());

            int total = filtered.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);
            List<Organization> paginatedList = filtered.subList(start, end);

            Page<Organization> resultPage = new PageImpl<>(paginatedList, PageRequest.of(page, size), total);
            return ResponseEntity.ok(ApiResponse.success("Organizations retrieved successfully.", resultPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ORG_LIST_ERR"));
        }
    }

    @GetMapping("/{orgId}")
    @Operation(summary = "Get Organization Details")
    public ResponseEntity<?> getOrganizationDetails(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.organization.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.organization.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        return ResponseEntity.ok(ApiResponse.success("Organization details retrieved successfully.", org));
    }

    @GetMapping("/{orgId}/summary")
    @Operation(summary = "Get Organization Dashboard Summary Statistics")
    public ResponseEntity<?> getOrganizationSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.organization.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.organization.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        long userCount = userRepository.countByOrganizationId(orgId);
        List<Role> roles = roleRepository.findByOrganizationId(orgId);
        long roleCount = roles.size();

        long customRoleCount = roles.stream().filter(r -> !r.isSystemRole()).count();
        long permissionCount = roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .count();

        String subscriptionName = org.getActiveSubscription() != null ? org.getActiveSubscription().getPlanName() : "None";
        String status = org.isDeleted() ? "SUSPENDED" : "ACTIVE";

        PlatformOrganizationSummaryResponse summary = new PlatformOrganizationSummaryResponse(
                orgId,
                org.getName(),
                userCount,
                roleCount,
                customRoleCount,
                permissionCount,
                subscriptionName,
                status
        );

        return ResponseEntity.ok(ApiResponse.success("Organization summary statistics retrieved successfully.", summary));
    }

    @GetMapping("/{orgId}/rbac-summary")
    @Operation(summary = "Get Organization RBAC Summary")
    public ResponseEntity<?> getOrganizationRbacSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.organization.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.organization.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        long userCount = userRepository.countByOrganizationId(orgId);
        List<Role> roles = roleRepository.findByOrganizationId(orgId);
        long roleCount = roles.size();
        long customRoleCount = roles.stream().filter(r -> !r.isSystemRole()).count();

        // Calculate customized roles count (where permissions list differs from platform template)
        long customizedRolesCount = 0;
        for (Role r : roles) {
            Optional<Role> templateOpt = roleRepository.findByNameAndIsPlatformTemplateTrue(r.getName());
            if (templateOpt.isPresent()) {
                Set<String> rolePerms = r.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet());
                Set<String> templatePerms = templateOpt.get().getPermissions().stream().map(p -> p.getName()).collect(Collectors.toSet());
                if (!rolePerms.equals(templatePerms)) {
                    customizedRolesCount++;
                }
            } else {
                customizedRolesCount++;
            }
        }

        long totalUniquePermissions = roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .count();

        // Template version mapping
        int templateVersion = roles.stream().mapToInt(Role::getVersion).max().orElse(1);

        OrganizationRbacSummary summary = new OrganizationRbacSummary(
                userCount,
                roleCount,
                customRoleCount,
                totalUniquePermissions,
                customizedRolesCount,
                templateVersion
        );

        return ResponseEntity.ok(ApiResponse.success("Organization RBAC summary retrieved successfully.", summary));
    }

    @GetMapping("/{orgId}/audit-logs")
    @Operation(summary = "Get Organization Audit Logs")
    public ResponseEntity<?> getOrganizationAuditLogs(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.audit.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.audit.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        List<User> orgUsers = userRepository.findByOrganizationId(orgId);
        List<String> emails = orgUsers.stream().map(User::getWorkEmail).collect(Collectors.toList());

        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log -> emails.contains(log.getUserEmail()))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Organization audit logs retrieved successfully.", logs));
    }

    @GetMapping("/{orgId}/role-history")
    @Operation(summary = "Get Organization Role Audit History")
    public ResponseEntity<?> getOrganizationRoleHistory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.audit.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.audit.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        List<User> orgUsers = userRepository.findByOrganizationId(orgId);
        List<String> emails = orgUsers.stream().map(User::getWorkEmail).collect(Collectors.toList());

        List<AuditLog> roleLogs = auditLogRepository.findAll().stream()
                .filter(log -> emails.contains(log.getUserEmail()) && ("Role".equalsIgnoreCase(log.getEntityType()) || log.getAction().toLowerCase().contains("role")))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Organization role history retrieved successfully.", roleLogs));
    }

    @GetMapping("/{orgId}/activities")
    @Operation(summary = "Get Organization Activity Feed")
    public ResponseEntity<?> getOrganizationActivities(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long orgId) {

        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkPermission(user, "platform.organization.view")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'platform.organization.view' permission.", "AUTH_002"));
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        if (org.isDeleted()) {
            return organizationSuspendedResponse();
        }

        List<User> orgUsers = userRepository.findByOrganizationId(orgId);
        List<String> emails = orgUsers.stream().map(User::getWorkEmail).collect(Collectors.toList());

        List<Map<String, Object>> activities = auditLogRepository.findAll().stream()
                .filter(log -> emails.contains(log.getUserEmail()))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .limit(50)
                .map(log -> {
                    Map<String, Object> activity = new LinkedHashMap<>();
                    activity.put("timestamp", log.getCreatedAt().toString());
                    activity.put("action", log.getAction());
                    activity.put("details", log.getDetails() != null ? log.getDetails() : "");
                    activity.put("actor", log.getUserName() != null ? log.getUserName() : log.getUserEmail());
                    return activity;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Organization activity feed retrieved successfully.", activities));
    }
}
