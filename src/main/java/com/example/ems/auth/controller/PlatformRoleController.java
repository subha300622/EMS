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
@RequestMapping("/api/v1/platform/roles")
@CrossOrigin("*")
@Tag(name = "Platform Role Template Administration")
public class PlatformRoleController {

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

    @Operation(summary = "List Platform Role Templates")
    @GetMapping
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> listTemplates(@RequestHeader("Authorization") String authHeader) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        List<Role> templates = roleService.getPlatformTemplates();
        return ResponseEntity.ok(ApiResponse.success("Platform role templates retrieved successfully", templates));
    }

    @Operation(summary = "Create Platform Role Template")
    @PostMapping
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createTemplate(@RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid RoleRequest request) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        try {
            Role created = roleService.createPlatformTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Platform role template created successfully", created));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    @Operation(summary = "Get Platform Role Template Details")
    @GetMapping("/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTemplate(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        Role template = roleService.getRoleById(id)
                .filter(Role::isPlatformTemplate)
                .orElse(null);
        if (template == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Platform role template not found with ID: " + id, "ROLE_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Platform role template retrieved successfully", template));
    }

    @Operation(summary = "Update Platform Role Template")
    @PutMapping("/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateTemplate(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid RoleRequest request) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        try {
            Role updated = roleService.updatePlatformTemplate(id, request);
            return ResponseEntity.ok(ApiResponse.success("Platform role template updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }

    @Operation(summary = "Delete Platform Role Template")
    @DeleteMapping("/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteTemplate(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        try {
            roleService.deletePlatformTemplate(id);
            return ResponseEntity.ok(ApiResponse.success("Platform role template deleted successfully"));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_003"));
        }
    }

    @Operation(summary = "Get Platform Role Template Permissions")
    @GetMapping("/{id}/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTemplatePermissions(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        Role template = roleService.getRoleById(id)
                .filter(Role::isPlatformTemplate)
                .orElse(null);
        if (template == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Platform role template not found with ID: " + id, "ROLE_002"));
        }
        List<Map<String, Object>> perms = template.getPermissions().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "name", p.getName(), "description",
                        p.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity
                .ok(ApiResponse.success("Platform role template permissions retrieved successfully", perms));
    }

    @Operation(summary = "Update Platform Role Template Permissions")
    @PutMapping("/{id}/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateTemplatePermissions(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> request) {
        User admin = resolveAdmin(authHeader);
        if (admin == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires Super Admin privileges", "AUTH_002"));
        }
        List<Long> permissionIds = request.get("permissionIds");
        if (permissionIds == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Request body must contain 'permissionIds' array.", "ROLE_004"));
        }
        try {
            boolean success = roleService.assignPermissionIdsToRole(id, permissionIds);
            if (success) {
                return ResponseEntity
                        .ok(ApiResponse.success("Platform role template permissions updated successfully"));
            } else {
                return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Platform template not found", "ROLE_002"));
            }
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "ROLE_001"));
        }
    }
}
