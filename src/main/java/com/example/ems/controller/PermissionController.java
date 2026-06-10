package com.example.ems.controller;

import com.example.ems.dto.PermissionRequest;
import com.example.ems.entity.Permission;
import com.example.ems.service.PermissionService;
import com.example.ems.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
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
}
