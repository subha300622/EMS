package com.example.ems.auth.controller;

import com.example.ems.auth.dto.PermissionRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.service.PermissionService;
import com.example.ems.auth.service.RoleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Administration")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @PostMapping("/permissions")
    public ResponseEntity<?> createPermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @RequestBody @Valid PermissionRequest request) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        try {
            Permission created = permissionService.createPermission(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/permissions/{id}")
    public ResponseEntity<?> getPermissionById(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        return permissionService.getPermissionById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Permission not found with ID: " + id)));
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<?> updatePermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id,
            @RequestBody @Valid PermissionRequest request) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        try {
            return permissionService.updatePermission(id, request)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Permission not found with ID: " + id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<?> deletePermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        boolean deleted = permissionService.deletePermission(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Permission deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Permission not found with ID: " + id));
        }
    }
}
