package com.example.ems.maintenance.controller;

import com.example.ems.maintenance.dto.MaintenanceEnableRequest;
import com.example.ems.maintenance.dto.MaintenanceResponse;
import com.example.ems.maintenance.dto.MaintenanceUpdateRequest;
import com.example.ems.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/maintenance")
@Tag(name = "Platform Maintenance", description = "Global Platform Admin Maintenance Mode Management APIs")
public class PlatformMaintenanceController {

    private final MaintenanceService maintenanceService;

    public PlatformMaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PLATFORM_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Get global maintenance configuration and effective status")
    public ResponseEntity<MaintenanceResponse> getSettings() {
        return ResponseEntity.ok(maintenanceService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PLATFORM_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Update maintenance configuration")
    public ResponseEntity<MaintenanceResponse> updateSettings(@Valid @RequestBody MaintenanceUpdateRequest request) {
        return ResponseEntity.ok(maintenanceService.updateSettings(request));
    }

    @PostMapping("/enable")
    @PreAuthorize("hasAnyAuthority('ROLE_PLATFORM_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Enable or schedule maintenance mode")
    public ResponseEntity<MaintenanceResponse> enable(@Valid @RequestBody MaintenanceEnableRequest request) {
        return ResponseEntity.ok(maintenanceService.enable(request));
    }

    @PostMapping("/disable")
    @PreAuthorize("hasAnyAuthority('ROLE_PLATFORM_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Disable maintenance mode")
    public ResponseEntity<MaintenanceResponse> disable() {
        return ResponseEntity.ok(maintenanceService.disable());
    }
}
