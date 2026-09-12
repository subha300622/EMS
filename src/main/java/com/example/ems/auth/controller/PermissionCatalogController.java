package com.example.ems.auth.controller;

import com.example.ems.auth.dto.PermissionCatalogResponseDto;
import com.example.ems.auth.service.PermissionService;
import com.example.ems.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@CrossOrigin("*")
@Tag(name = "Permissions Catalog")
public class PermissionCatalogController {

    @Autowired
    private PermissionService permissionService;

    @Operation(summary = "Get System Permission Groups and Catalog", description = "Returns system permission groups and standalone permissions for dynamic web/mobile role creation UI.")
    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<PermissionCatalogResponseDto>> getPermissionCatalog() {
        PermissionCatalogResponseDto catalog = permissionService.getPermissionCatalog();
        return ResponseEntity.ok(ApiResponse.success("Permission catalog retrieved successfully", catalog));
    }
}
