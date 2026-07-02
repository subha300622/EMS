package com.example.ems.auth.controller;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.PermissionRepository;
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

@RestController
@RequestMapping("/api/v1/platform/permissions")
@CrossOrigin("*")
@Tag(name = "Platform Permission Administration")
public class PermissionController {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveAdmin(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email)
                        .filter(u -> roleService.isSuperAdmin(email))
                        .orElse(null);
            }
        }
        return null;
    }

    @Operation(summary = "List All System Permissions")
    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listPermissions(@RequestHeader("Authorization") String authHeader) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        List<Permission> permissions = permissionRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("System permissions retrieved successfully", permissions));
    }

    @Operation(summary = "Create System Permission")
    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createPermission(@RequestHeader("Authorization") String authHeader,
                                                                @RequestBody @Valid Permission request) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        if (permissionRepository.existsByName(request.getName())) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Permission with name '" + request.getName() + "' already exists", "PERM_001"));
        }
        Permission permission = new Permission();
        permission.setName(request.getName().trim());
        permission.setDescription(request.getDescription());
        Permission saved = permissionRepository.save(permission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("System permission created successfully", saved));
    }

    @Operation(summary = "Update System Permission")
    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updatePermission(@RequestHeader("Authorization") String authHeader,
                                                                @PathVariable Long id,
                                                                @RequestBody @Valid Permission request) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Permission not found with ID: " + id, "PERM_002"));
        }
        if (!permission.getName().equalsIgnoreCase(request.getName()) && permissionRepository.existsByName(request.getName())) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Permission with name '" + request.getName() + "' already exists", "PERM_001"));
        }
        permission.setName(request.getName().trim());
        permission.setDescription(request.getDescription());
        Permission saved = permissionRepository.save(permission);
        return ResponseEntity.ok(ApiResponse.success("System permission updated successfully", saved));
    }

    @Operation(summary = "Delete System Permission")
    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deletePermission(@RequestHeader("Authorization") String authHeader,
                                                                @PathVariable Long id) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        if (!permissionRepository.existsById(id)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Permission not found with ID: " + id, "PERM_002"));
        }
        permissionRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("System permission deleted successfully"));
    }
}
