package com.example.ems.controller;

import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.dto.UserCreateRequest;
import com.example.ems.dto.UserUpdateRequest;
import com.example.ems.dto.AssignRoleRequest;
import com.example.ems.dto.UpdateStatusRequest;
import com.example.ems.entity.User;
import com.example.ems.repository.UserRepository;
import com.example.ems.service.JwtService;
import com.example.ems.service.RoleService;
import com.example.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
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

    private boolean checkUserManagePermission(User currentUser) {
        return roleService.hasPermission(currentUser.getWorkEmail(), "user.manage");
    }

    // ── 1. Create User ───────────────────────────────────────────────────────
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid UserCreateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        try {
            User created = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_001"));
        }
    }

    // ── 2. Get All Users ─────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    // ── 3. Get User By ID ────────────────────────────────────────────────────
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        return userService.getUserById(id)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("User not found with ID: " + id, "USR_002")));
    }

    // ── 4. Update User ───────────────────────────────────────────────────────
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        try {
            return userService.updateUser(id, request)
                    .<ResponseEntity<?>>map(user -> ResponseEntity.ok(ApiResponse.success("User updated successfully", user)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ErrorResponse.error("User not found with ID: " + id, "USR_002")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_003"));
        }
    }

    // ── 5. Delete User ───────────────────────────────────────────────────────
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + id, "USR_002"));
        }
    }

    // ── 6. Update User Role ──────────────────────────────────────────────────
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid AssignRoleRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        try {
            boolean updated = userService.updateUserRole(id, request.getRole());
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success("User role updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("User not found with ID: " + id, "USR_002"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_004"));
        }
    }

    // ── 7. Update User Status ────────────────────────────────────────────────
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid UpdateStatusRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        boolean updated = userService.updateUserStatus(id, request.getStatus());
        if (updated) {
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("User not found with ID: " + id, "USR_002"));
        }
    }

    // ── 8. Search Users ──────────────────────────────────────────────────────
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "query", required = false) String query) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!checkUserManagePermission(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.manage' permission.", "AUTH_002"));
        }

        List<User> results = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }
}
