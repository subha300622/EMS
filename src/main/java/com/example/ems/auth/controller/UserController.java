package com.example.ems.auth.controller;

import com.example.ems.auth.dto.UserManagementDtos.*;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.common.util.OrganizationIdResolver;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
@Tag(name = "User Management", description = "System user accounts, lifecycle administration, user search.")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // ── Helper: Authenticate & Resolve Caller ─────────────────────────────────
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
        if (user == null) return false;
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        return "SUPER_ADMIN".equalsIgnoreCase(roleName)
                || "PLATFORM_ADMIN".equalsIgnoreCase(roleName)
                || "SUPER_ADMIN".equalsIgnoreCase(user.getStatus())
                || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private boolean isTenantAdmin(User user) {
        if (user == null) return false;
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        return "ADMIN".equalsIgnoreCase(roleName)
                || "HR_MANAGER".equalsIgnoreCase(roleName)
                || "HR".equalsIgnoreCase(roleName);
    }

    private boolean hasUserPermission(User user, String permission) {
        if (user == null) return false;
        if (isPlatformAdmin(user) || isTenantAdmin(user)) return true;
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "user.manage");
    }

    private Optional<User> findTargetUser(String userId) {
        if (userId == null || userId.isBlank()) return Optional.empty();
        Optional<User> opt = userRepository.findByUserId(userId);
        if (opt.isEmpty() && userId.matches("\\d+")) {
            opt = userRepository.findById(Long.parseLong(userId));
        }
        return opt;
    }

    private boolean checkTenantBoundary(User caller, User target) {
        if (caller == null || target == null) return false;
        if (isPlatformAdmin(caller)) return true;

        Long callerOrgId = caller.getOrganization() != null ? caller.getOrganization().getId() : caller.getOrganizationId();
        Long targetOrgId = target.getOrganization() != null ? target.getOrganization().getId() : target.getOrganizationId();

        return callerOrgId != null && callerOrgId.equals(targetOrgId);
    }

    private String formatOrgId(Long orgId) {
        if (orgId == null) return null;
        return OrganizationIdResolver.formatId(orgId);
    }

    private String formatRoleId(Role role) {
        if (role == null || role.getId() == null) return "ROLE-001";
        return String.format("ROLE-%03d", role.getId());
    }

    private List<RoleItemDto> buildRolesList(User user) {
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        Role r = user.getRole();
        return List.of(new RoleItemDto(formatRoleId(r), r.getName()));
    }

    private Role resolveRole(String roleIdentifier, Long orgId) {
        if (roleIdentifier == null || roleIdentifier.isBlank()) return null;
        String clean = roleIdentifier.trim();

        // 1. Check numeric ID
        Long numericId = null;
        if (clean.matches("\\d+")) {
            numericId = Long.parseLong(clean);
        } else if (clean.toUpperCase().startsWith("ROLE-") || clean.toUpperCase().startsWith("ROLE_")) {
            String sub = clean.substring(5).trim();
            if (sub.matches("\\d+")) {
                numericId = Long.parseLong(sub);
            }
        }

        if (numericId != null) {
            Optional<Role> roleOpt = roleRepository.findById(numericId);
            if (roleOpt.isPresent()) return roleOpt.get();
        }

        // 2. Lookup by name variants (e.g. "HR_MANAGER", "ROLE-HR-MANAGER", "ROLE_HR_MANAGER")
        List<String> nameVariants = new ArrayList<>();
        nameVariants.add(clean);
        nameVariants.add(clean.toUpperCase());
        nameVariants.add(clean.replace("-", "_"));
        nameVariants.add(clean.toUpperCase().replace("-", "_"));
        if (clean.toUpperCase().startsWith("ROLE-") || clean.toUpperCase().startsWith("ROLE_")) {
            String stripped = clean.substring(5).trim();
            nameVariants.add(stripped);
            nameVariants.add(stripped.toUpperCase());
            nameVariants.add(stripped.replace("-", "_"));
            nameVariants.add(stripped.toUpperCase().replace("-", "_"));
        }

        for (String name : nameVariants) {
            if (orgId != null) {
                Optional<Role> tenantRole = roleRepository.findByOrganizationIdAndName(orgId, name);
                if (tenantRole.isPresent()) return tenantRole.get();
            }
            Optional<Role> globalRole = roleRepository.findByName(name);
            if (globalRole.isPresent()) return globalRole.get();
        }

        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. GET /api/v1/users/{userId} - Get User by ID
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get User by ID", description = "Retrieves user details under the tenant organization without permissions field.")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        Long orgId = target.getOrganization() != null ? target.getOrganization().getId() : target.getOrganizationId();
        String orgName = target.getOrganization() != null ? target.getOrganization().getName() : target.getOrganizationName();

        UserDetailResponse responseData = new UserDetailResponse(
                target.getUserId(),
                target.getEmployeeId() != null ? target.getEmployeeId() : target.getUserId(),
                formatOrgId(orgId),
                orgName,
                target.getFullName(),
                target.getWorkEmail(),
                target.getMobileNumber(),
                target.getStatus(),
                buildRolesList(target)
        );

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", responseData));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. PUT /api/v1/users/{userId} - Update User
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update User Details", description = "Updates profile fields: fullName, email, mobile. Disallows status, roles, password, and IDs.")
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.update permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            target.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            target.setWorkEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            target.setMobileNumber(request.getMobile().trim());
        }

        target = userRepository.save(target);

        UpdatedUserDataResponse data = new UpdatedUserDataResponse(
                target.getUserId(),
                target.getFullName(),
                target.getWorkEmail(),
                target.getMobileNumber()
        );

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. DELETE /api/v1/users/{userId} - Delete User (Soft Delete / Deactivation)
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Delete User", description = "Soft deletes / deactivates a user account.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.delete permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        target.setStatus("INACTIVE");
        userRepository.save(target);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. PUT /api/v1/users/{userId}/password/reset - Reset User Password
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Reset User Password", description = "Admin-initiated password reset generating a temporary password.")
    @PutMapping("/{userId}/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody(required = false) ResetPasswordAdminRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.password.reset") && !hasUserPermission(currentUser, "user.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires password reset permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        String rawPassword = UUID.randomUUID().toString().substring(0, 10) + "A1!";
        if (request != null && request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            rawPassword = request.getNewPassword();
        }

        if (passwordEncoder != null) {
            target.setPassword(passwordEncoder.encode(rawPassword));
        } else {
            target.setPassword(rawPassword);
        }
        userRepository.save(target);

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. PUT /api/v1/users/{userId}/role - Update User Role
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update User Role", description = "Updates a user's single primary role.")
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody UpdateRoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.role.assign permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        Long orgId = target.getOrganization() != null ? target.getOrganization().getId() : target.getOrganizationId();
        Role role = resolveRole(request.getRoleId(), orgId);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("Role not found with ID: " + request.getRoleId(), "ROLE_002"));
        }

        target.setRole(role);
        target.setRoleId(role.getId());
        userRepository.save(target);

        RoleItemDto roleItem = new RoleItemDto(request.getRoleId() != null ? request.getRoleId() : formatRoleId(role), role.getName());
        UserRoleUpdatedDataResponse data = new UserRoleUpdatedDataResponse(target.getUserId(), roleItem);

        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. DELETE /api/v1/users/{userId}/role - Remove User Role
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Remove User Role", description = "Removes any assigned role from the user.")
    @DeleteMapping("/{userId}/role")
    public ResponseEntity<?> removeUserRole(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.role.assign permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        target.setRole(null);
        target.setRoleId(null);
        userRepository.save(target);

        return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. GET /api/v1/users/{userId}/roles - Get User Roles
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get User Roles", description = "Retrieves user roles array without permissions.")
    @GetMapping("/{userId}/roles")
    public ResponseEntity<?> getUserRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        UserRolesResponse data = new UserRolesResponse(target.getUserId(), buildRolesList(target));
        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. PUT /api/v1/users/{userId}/roles - Assign Multiple Roles
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Assign Multiple Roles", description = "Assigns multiple roles to a user.")
    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> assignMultipleRoles(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody AssignMultipleRolesRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.role.assign")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.role.assign permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        Long orgId = target.getOrganization() != null ? target.getOrganization().getId() : target.getOrganizationId();

        List<RoleItemDto> assignedRoles = new ArrayList<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (String rid : request.getRoleIds()) {
                Role r = resolveRole(rid, orgId);
                if (r != null) {
                    target.setRole(r);
                    target.setRoleId(r.getId());
                    assignedRoles.add(new RoleItemDto(rid, r.getName()));
                    break; // Primary mapping
                }
            }
            userRepository.save(target);
        }

        UserRolesResponse data = new UserRolesResponse(target.getUserId(), assignedRoles.isEmpty() ? buildRolesList(target) : assignedRoles);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. PUT /api/v1/users/{userId}/status - Update User Status
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Update User Status", description = "Updates user status: ACTIVE, INACTIVE, SUSPENDED, LOCKED, PENDING.")
    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody UpdateStatusRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.update permission", "AUTH_002"));
        }

        Optional<User> targetOpt = findTargetUser(userId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User target = targetOpt.get();
        if (!checkTenantBoundary(currentUser, target)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: User belongs to another organization", "AUTH_002"));
        }

        target.setStatus(request.getStatus().toUpperCase());
        userRepository.save(target);

        UserStatusUpdatedDataResponse data = new UserStatusUpdatedDataResponse(target.getUserId(), target.getStatus());
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. GET /api/v1/users/export - Export Users to CSV
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Export Users to CSV", description = "Exports filtered user list as CSV.")
    @GetMapping("/export")
    public ResponseEntity<?> exportUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) String departmentId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        boolean isPlatAdmin = isPlatformAdmin(currentUser);

        List<User> list = userRepository.findAll().stream()
                .filter(u -> isPlatAdmin || (orgId != null && (u.getOrganization() != null ? u.getOrganization().getId().equals(orgId) : orgId.equals(u.getOrganizationId()))))
                .filter(u -> status == null || status.equalsIgnoreCase(u.getStatus()))
                .filter(u -> roleId == null || (u.getRole() != null && (roleId.equalsIgnoreCase(formatRoleId(u.getRole())) || roleId.equalsIgnoreCase(u.getRole().getName()))))
                .collect(Collectors.toList());

        StringBuilder csv = new StringBuilder("User ID,Employee ID,Full Name,Email,Status,Role\n");
        for (User u : list) {
            String empId = u.getEmployeeId() != null ? u.getEmployeeId() : (u.getUserId() != null ? u.getUserId() : "");
            String roleName = u.getRole() != null ? u.getRole().getName() : "";
            csv.append(u.getUserId() != null ? u.getUserId() : "").append(",")
               .append(empId).append(",")
               .append(u.getFullName() != null ? u.getFullName() : "").append(",")
               .append(u.getWorkEmail() != null ? u.getWorkEmail() : "").append(",")
               .append(u.getStatus() != null ? u.getStatus() : "").append(",")
               .append(roleName).append("\n");
        }

        byte[] data = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "users.csv");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. GET /api/v1/users/me/bootstrap - Current User Bootstrap (No Permissions)
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Current User Bootstrap Data", description = "Returns profile, organization, and roles without permissions field.")
    @GetMapping("/me/bootstrap")
    public ResponseEntity<?> getMyBootstrap(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        String orgName = currentUser.getOrganization() != null ? currentUser.getOrganization().getName() : currentUser.getOrganizationName();

        UserSummaryDto userSummary = new UserSummaryDto(
                currentUser.getUserId(),
                currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : currentUser.getUserId(),
                currentUser.getFullName(),
                currentUser.getWorkEmail(),
                currentUser.getMobileNumber(),
                currentUser.getStatus()
        );

        OrgSummaryDto orgSummary = new OrgSummaryDto(formatOrgId(orgId), orgName);

        BootstrapDataResponse response = new BootstrapDataResponse(userSummary, orgSummary, buildRolesList(currentUser));
        return ResponseEntity.ok(ApiResponse.success("Bootstrap data retrieved successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 12. GET /api/v1/users/me/context - Current User Organization Context
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Current User Org Context", description = "Returns organization scope and details for current user.")
    @GetMapping("/me/context")
    public ResponseEntity<?> getMyContext(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        boolean isPlatAdmin = isPlatformAdmin(currentUser);
        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        String orgName = currentUser.getOrganization() != null ? currentUser.getOrganization().getName() : currentUser.getOrganizationName();

        ContextDataResponse data;
        if (isPlatAdmin && orgId == null) {
            data = new ContextDataResponse(currentUser.getUserId(), null, null, "PLATFORM");
        } else {
            data = new ContextDataResponse(currentUser.getUserId(), formatOrgId(orgId), orgName, "ORGANIZATION");
        }

        return ResponseEntity.ok(ApiResponse.success("User organization context retrieved successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 13. GET /api/v1/users/me/profile - Current User Profile (No Permissions)
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Current User Profile", description = "Returns profile details, organization, and roles.")
    @GetMapping("/me/profile")
    public ResponseEntity<?> getMyProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        String orgName = currentUser.getOrganization() != null ? currentUser.getOrganization().getName() : currentUser.getOrganizationName();

        UserProfileResponse data = new UserProfileResponse(
                currentUser.getUserId(),
                currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : currentUser.getUserId(),
                currentUser.getFullName(),
                currentUser.getWorkEmail(),
                currentUser.getMobileNumber(),
                null,
                new OrgSummaryDto(formatOrgId(orgId), orgName),
                buildRolesList(currentUser)
        );

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", data));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 14. GET /api/v1/users/pending - Get Pending Users
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get Pending Users", description = "Retrieves registration requests pending approval scoped to current tenant.")
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        boolean isPlatAdmin = isPlatformAdmin(currentUser);

        List<PendingUserDto> pendingUsers = userRepository.findAll().stream()
                .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                .filter(u -> isPlatAdmin || (orgId != null && (u.getOrganization() != null ? u.getOrganization().getId().equals(orgId) : orgId.equals(u.getOrganizationId()))))
                .map(u -> new PendingUserDto(
                        u.getUserId(),
                        u.getFullName(),
                        u.getWorkEmail(),
                        "PENDING",
                        u.getCreatedAt() != null ? u.getCreatedAt().toString() : Instant.now().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Pending users retrieved successfully", pendingUsers));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 15. GET /api/v1/users/search - Search Users
    // ─────────────────────────────────────────────────────────────────────────
    @Operation(summary = "Search Users", description = "Paginated search across users with query, status, and roleId filters.")
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!hasUserPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.error("Access Denied: Requires user.read permission", "AUTH_002"));
        }

        Long orgId = currentUser.getOrganization() != null ? currentUser.getOrganization().getId() : currentUser.getOrganizationId();
        boolean isPlatAdmin = isPlatformAdmin(currentUser);

        String q = query != null ? query.trim().toLowerCase() : "";

        List<User> matching = userRepository.findAll().stream()
                .filter(u -> isPlatAdmin || (orgId != null && (u.getOrganization() != null ? u.getOrganization().getId().equals(orgId) : orgId.equals(u.getOrganizationId()))))
                .filter(u -> status == null || status.equalsIgnoreCase(u.getStatus()))
                .filter(u -> roleId == null || (u.getRole() != null && (roleId.equalsIgnoreCase(formatRoleId(u.getRole())) || roleId.equalsIgnoreCase(u.getRole().getName()))))
                .filter(u -> q.isEmpty()
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                        || (u.getWorkEmail() != null && u.getWorkEmail().toLowerCase().contains(q))
                        || (u.getUserId() != null && u.getUserId().toLowerCase().contains(q)))
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());

        int total = matching.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<User> paginated = matching.subList(start, end);

        List<SearchUserItemDto> content = paginated.stream()
                .map(u -> new SearchUserItemDto(
                        u.getUserId(),
                        u.getEmployeeId() != null ? u.getEmployeeId() : u.getUserId(),
                        u.getFullName(),
                        u.getWorkEmail(),
                        u.getStatus(),
                        buildRolesList(u)
                ))
                .collect(Collectors.toList());

        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        PaginatedSearchResponse data = new PaginatedSearchResponse(content, page, size, total, totalPages);

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
    }
}
