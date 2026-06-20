package com.example.ems.auth.controller;

import com.example.ems.auth.dto.AssignRoleRequest;
import com.example.ems.auth.dto.UpdateStatusRequest;
import com.example.ems.auth.dto.UserCreateRequest;
import com.example.ems.auth.dto.UserUpdateRequest;
import com.example.ems.auth.dto.AdminResetPasswordRequest;
import com.example.ems.auth.dto.ProfileUpdateRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.auth.service.UserService;
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

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "User Management")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // Helper: Resolve currently authenticated User via JWT only
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

    private boolean hasPermission(User user, String permission) {
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "user.manage");
    }

    private Map<String, Object> formatCreatedUser(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("workEmail", user.getWorkEmail());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("employeeId", user.getEmployeeId());
        data.put("department", user.getDepartment());
        if (user.getRole() != null) {
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("id", user.getRole().getId());
            roleMap.put("name", user.getRole().getName());
            roleMap.put("description", user.getRole().getDescription());
            data.put("role", roleMap);
        } else {
            data.put("role", null);
        }
        data.put("location", user.getLocation());
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt());
        return data;
    }

    private Map<String, Object> formatUserDetail(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("workEmail", user.getWorkEmail());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("employeeId", user.getEmployeeId());
        data.put("department", user.getDepartment());
        if (user.getRole() != null) {
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("id", user.getRole().getId());
            roleMap.put("name", user.getRole().getName());
            data.put("role", roleMap);
        } else {
            data.put("role", null);
        }
        data.put("location", user.getLocation());
        data.put("status", user.getStatus());
        return data;
    }

    private Map<String, Object> formatUserUpdateDetail(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("workEmail", user.getWorkEmail());
        data.put("mobileNumber", user.getMobileNumber());
        data.put("department", user.getDepartment());
        data.put("location", user.getLocation());
        data.put("status", user.getStatus());
        return data;
    }

    // ── 1. Create User ───────────────────────────────────────────────────────
    @Operation(summary = "Create User", description = "Creates a new user account with specified employee details, roles, and status.")
    @PostMapping("/users")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid UserCreateRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.create")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.create' or 'user.manage' permission.", "AUTH_002"));
        }

        try {
            User created = userService.createUser(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", formatCreatedUser(created)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_001"));
        }
    }

    // ── 2. Get All Users ─────────────────────────────────────────────────────
    @Operation(summary = "Get All Users", description = "Retrieves a list of all user accounts, with optional status filtering (e.g., ACTIVE, PENDING).")
    @GetMapping("/users")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", required = false) String statusFilter){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (User u : users) {
            // Apply optional status filter: /users?status=PENDING or ?status=ACTIVE
            if (statusFilter != null && !statusFilter.isBlank()
                    && !statusFilter.equalsIgnoreCase(u.getStatus())) {
                continue;
            }
            Map<String, Object> uMap = new LinkedHashMap<>();
            uMap.put("id", u.getId());
            uMap.put("userId", u.getUserId());
            uMap.put("fullName", u.getFullName());
            uMap.put("email", u.getWorkEmail());
            uMap.put("department", u.getDepartment());
            uMap.put("role", u.getRole() != null ? u.getRole().getName() : null);
            uMap.put("status", u.getStatus());
            formattedList.add(uMap);
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", formattedList));
    }

    // ── 2b. Get Pending Users (awaiting admin approval) ───────────────────────
    @Operation(summary = "Get Pending Users", description = "Retrieves a list of user registration requests that are pending admin approval.")
    @GetMapping("/users/pending")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getPendingUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        List<User> pendingUsers = userService.getAllUsers().stream()
                .filter(u -> "PENDING".equalsIgnoreCase(u.getStatus()))
                .toList();

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (User u : pendingUsers) {
            Map<String, Object> uMap = new LinkedHashMap<>();
            uMap.put("id", u.getId());
            uMap.put("userId", u.getUserId());
            uMap.put("fullName", u.getFullName());
            uMap.put("workEmail", u.getWorkEmail());
            uMap.put("department", u.getDepartment());
            uMap.put("mobileNumber", u.getMobileNumber());
            uMap.put("role", null);
            uMap.put("status", "PENDING");
            uMap.put("registeredAt", u.getCreatedAt());
            formattedList.add(uMap);
        }
        return ResponseEntity.ok(ApiResponse.success(
                pendingUsers.size() + " user(s) pending admin approval", formattedList));
    }

    // ── 3. Get User By ID ────────────────────────────────────────────────────
    @Operation(summary = "Get User by ID", description = "Retrieves detailed information of a specific user account using their unique user ID.")
    @GetMapping("/users/{userId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) userService.getUserByUserId(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", formatUserDetail(user))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002")));
    }

    // ── 4. Update User ───────────────────────────────────────────────────────
    @Operation(summary = "Update User Details", description = "Updates profile fields such as name, contact number, department, and location for a specific user.")
    @PutMapping("/users/{userId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ErrorResponse> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody @Valid UserUpdateRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.update")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.update' or 'user.manage' permission.", "AUTH_002"));
        }

        try {
            return (ResponseEntity) userService.updateUserByUserId(userId, request)
                    .<ResponseEntity<?>>map(user -> ResponseEntity.ok(ApiResponse.success("User updated successfully", formatUserUpdateDetail(user))))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002")));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_003"));
        }
    }

    // ── 5. Delete User ───────────────────────────────────────────────────────
    @Operation(summary = "Delete User", description = "Permanently deletes a user account from the system.")
    @DeleteMapping("/users/{userId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.delete")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.delete' or 'user.manage' permission.", "AUTH_002"));
        }

        boolean deleted = userService.deleteUserByUserId(userId);
        if (deleted) {
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("userId", userId);
            responseData.put("deleted", true);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", responseData));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }
    }

    // ── 6. Update User Role ──────────────────────────────────────────────────
    @Operation(summary = "Update User Role", description = "Assigns a specific system security role to a user account.")
    @PutMapping("/users/{userId}/role")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody @Valid AssignRoleRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.role.assign")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.role.assign' or 'user.manage' permission.", "AUTH_002"));
        }

        try {
            boolean updated = userService.updateUserRoleByUserId(userId, request.getRole());
            if (updated) {
                Map<String, Object> responseData = new LinkedHashMap<>();
                responseData.put("userId", userId);
                responseData.put("role", request.getRole());
                return ResponseEntity.ok(ApiResponse.success("User role updated successfully", responseData));
            } else {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
            }
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_004"));
        }
    }

    // ── 7. Update User Status ────────────────────────────────────────────────
    @Operation(summary = "Update User Status", description = "Changes the account status of a user (e.g., activating or deactivating the account).")
    @PutMapping("/users/{userId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody @Valid UpdateStatusRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.update")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.update' or 'user.manage' permission.", "AUTH_002"));
        }

        boolean updated = userService.updateUserStatusByUserId(userId, request.getStatus());
        if (updated) {
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("userId", userId);
            responseData.put("status", request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", responseData));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }
    }

    // ── 7b. Delete User Role ─────────────────────────────────────────────────
    @Operation(summary = "Remove User Role", description = "Clears and removes any assigned security role from the user's account.")
    @DeleteMapping("/users/{userId}/role")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.role.assign")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.role.assign' or 'user.manage' permission.", "AUTH_002"));
        }

        boolean updated = userService.removeUserRoleByUserId(userId);
        if (updated) {
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("userId", userId);
            responseData.put("role", null);
            return ResponseEntity.ok(ApiResponse.success("User role removed successfully", responseData));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }
    }

    // ── 7c. Get User Roles ───────────────────────────────────────────────────
    @Operation(summary = "Get User Roles", description = "Retrieves the security roles and associated access permissions mapped to a user account.")
    @GetMapping("/users/{userId}/roles")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getUserRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        java.util.Optional<User> targetUser = userService.getUserByUserId(userId);
        if (targetUser.isEmpty()) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + userId, "USR_002"));
        }

        User user = targetUser.get();
        List<Map<String, Object>> rolesList = new ArrayList<>();
        if (user.getRole() != null) {
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", user.getRole().getId());
            roleMap.put("name", user.getRole().getName());
            roleMap.put("description", user.getRole().getDescription());
            
            List<Map<String, Object>> permsList = new ArrayList<>();
            if (user.getRole().getPermissions() != null) {
                for (com.example.ems.auth.entity.Permission p : user.getRole().getPermissions()) {
                    Map<String, Object> pMap = new LinkedHashMap<>();
                    pMap.put("permissionId", p.getId());
                    pMap.put("name", p.getName());
                    permsList.add(pMap);
                }
            }
            roleMap.put("permissions", permsList);
            rolesList.add(roleMap);
        }
        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", rolesList));
    }

    // ── 8. Search Users ──────────────────────────────────────────────────────
    @Operation(summary = "Search Users", description = "Searches for user accounts matching a query string in fields like name, email, and department.")
    @GetMapping("/users/search")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> searchUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "query", required = false) String query){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        List<User> results = userService.searchUsers(query);
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (User u : results) {
            Map<String, Object> uMap = new LinkedHashMap<>();
            uMap.put("userId", u.getUserId());
            uMap.put("fullName", u.getFullName());
            uMap.put("workEmail", u.getWorkEmail());
            uMap.put("department", u.getDepartment());
            uMap.put("status", u.getStatus());
            formattedList.add(uMap);
        }
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", formattedList));
    }


    // ── 11. Reset Password (Admin) ───────────────────────────────────────────
    @Operation(summary = "Reset Password (Admin)", description = "Admin-initiated password reset that updates the target user's password without verification checks.")
    @PutMapping("/users/{userId}/password/reset")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody @Valid AdminResetPasswordRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.password.reset")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.password.reset' or 'user.manage' permission.", "AUTH_002"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("Passwords do not match", "AUTH_005"));
        }

        try {
            userService.resetUserPasswordByUserId(userId, request.getNewPassword());
            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("userId", userId);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully", responseData));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "USR_002"));
        }
    }

    // ── 12. Export Users ─────────────────────────────────────────────────────
    @Operation(summary = "Export Users to CSV", description = "Generates and downloads a CSV report of all registered user accounts with their metadata.")
    @GetMapping("/users/export")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<byte[]> exportUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        List<User> list = userService.getAllUsers();
        StringBuilder csv = new StringBuilder("User ID,Full Name,Email,Department,Role,Status\n");
        for (User u : list) {
            csv.append(u.getUserId()).append(",")
               .append(u.getFullName()).append(",")
               .append(u.getWorkEmail()).append(",")
               .append(u.getDepartment() != null ? u.getDepartment() : "").append(",")
               .append(u.getRole() != null ? u.getRole().getName() : u.getRequestedRole() != null ? u.getRequestedRole() : "").append(",")
               .append(u.getStatus() != null ? u.getStatus() : "").append("\n");
        }

        byte[] data = csv.toString().getBytes();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users.csv");
        headers.setContentLength(data.length);

        return (ResponseEntity) new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
