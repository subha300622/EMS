package com.example.ems.auth.controller;

import java.util.List;

import com.example.ems.auth.dto.PermissionRequest;
import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.service.PermissionService;
import com.example.ems.auth.service.RoleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Permission Management")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RoleService roleService;

    @Operation(summary = "Create Permission", description = "Creates a new granular security access permission in the system catalog.")
    @PostMapping("/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<Permission> createPermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @RequestBody @Valid PermissionRequest request) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        try {
            Permission created = permissionService.createPermission(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Get All Permissions", description = "Retrieves a list of all defined security permissions in the system catalog.")
    @GetMapping("/permissions")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<List<Permission>> getPermissions(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @Operation(summary = "Get Permission Details", description = "Retrieves the detail information of a specific security permission by ID.")
    @GetMapping("/permissions/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<Map<String, Object>> getPermissionById(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        return (ResponseEntity) permissionService.getPermissionById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Permission not found with ID: " + id)));
    }

    @Operation(summary = "Update Permission", description = "Updates the name and description of an existing security permission.")
    @PutMapping("/permissions/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<Map<String, Object>> updatePermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id,
            @RequestBody @Valid PermissionRequest request) {

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        try {
            return (ResponseEntity) permissionService.updatePermission(id, request)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Permission not found with ID: " + id)));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Delete Permission", description = "Deletes a security permission from the system catalog.")
    @DeleteMapping("/permissions/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Map<String, Object>> deletePermission(
            @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
            @PathVariable Long id){

        if (!roleService.hasPermission(adminEmail, "permission.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Requires 'permission.manage' permission."));
        }

        boolean deleted = permissionService.deletePermission(id);
        if (deleted) {
            return (ResponseEntity) ResponseEntity.ok(Map.of("message", "Permission deleted successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Permission not found with ID: " + id));
        }
    }
}
