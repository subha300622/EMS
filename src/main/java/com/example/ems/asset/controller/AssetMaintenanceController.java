package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.MaintenanceStatus;
import com.example.ems.asset.service.AssetMaintenanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-maintenances")
@CrossOrigin("*")
@Tag(name = "Asset Maintenance Operations")
public class AssetMaintenanceController {

    @Autowired
    private AssetMaintenanceService maintenanceService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String principal = SecurityContextHolder.getContext().getAuthentication().getName();
            if (principal != null && !principal.isBlank()) {
                return userRepository.findByWorkEmail(principal).orElse(null);
            }
        }
        return null;
    }

    private Long resolveOrgId(User user) {
        if (user == null || user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized or missing organization context");
        }
        return user.getOrganizationId();
    }

    private String getPerformedBy(User user) {
        if (user != null) {
            return user.getWorkEmail() != null ? user.getWorkEmail() : "User " + user.getId();
        }
        return "System User";
    }

    @PostMapping("/assets/{assetId}")
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_CREATE')")
    public ResponseEntity<AssetActionResultResponse> scheduleMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId,
            @Valid @RequestBody CreateMaintenanceRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = maintenanceService.scheduleMaintenance(orgId, assetId, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_START')")
    public ResponseEntity<AssetMaintenanceResponse> startMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetMaintenanceResponse response = maintenanceService.startMaintenance(orgId, id, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_COMPLETE')")
    public ResponseEntity<AssetMaintenanceResponse> completeMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetMaintenanceResponse response = maintenanceService.completeMaintenance(orgId, id, request, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_CANCEL')")
    public ResponseEntity<AssetMaintenanceResponse> cancelMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetMaintenanceResponse response = maintenanceService.cancelMaintenance(orgId, id, remarks, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assets/{assetId}")
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_VIEW')")
    public ResponseEntity<List<AssetMaintenanceResponse>> getMaintenancesByAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<AssetMaintenanceResponse> list = maintenanceService.getMaintenancesByAsset(orgId, assetId);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_MAINTENANCE_VIEW')")
    public ResponseEntity<List<AssetMaintenanceResponse>> getMaintenancesByStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam MaintenanceStatus status) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<AssetMaintenanceResponse> list = maintenanceService.getMaintenancesByStatus(orgId, status);
        return ResponseEntity.ok(list);
    }
}
