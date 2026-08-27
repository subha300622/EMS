package com.example.ems.auth.controller;

import com.example.ems.auth.dto.AdminUserDtos;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.AdminUserService;
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

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*")
@Tag(name = "Admin User Management")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

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

    private boolean hasPermission(User user, String permission) {
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.hasPermission(user.getWorkEmail(), "user.manage");
    }

    @Operation(summary = "Create Tenant User", description = "Creates a new user under the current tenant organization.")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid AdminUserDtos.CreateAdminUserRequest request) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.create' or 'user.manage' permission.", "AUTH_002"));
        }

        try {
            AdminUserDtos.AdminUserResponse response = adminUserService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", response));
        } catch (com.example.ems.common.exception.BadRequestException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "USR_001"));
        } catch (com.example.ems.common.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "USR_002"));
        }
    }

    @Operation(summary = "Get Tenant User by ID", description = "Retrieves user details under the current tenant organization.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        try {
            AdminUserDtos.AdminUserResponse response = adminUserService.getUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", response));
        } catch (com.example.ems.common.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "USR_002"));
        }
    }

    @Operation(summary = "List Tenant Users", description = "Lists all users under the current tenant organization.")
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!hasPermission(currentUser, "user.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'user.read' or 'user.manage' permission.", "AUTH_002"));
        }

        List<AdminUserDtos.AdminUserResponse> response = adminUserService.listUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
}
