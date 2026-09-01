package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.service.AssetHistoryService;
import com.example.ems.asset.service.AssetLifecycleService;
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
@RequestMapping("/api/v1/assets")
@CrossOrigin("*")
@Tag(name = "Asset Master Operations")
public class AssetManagementController {

    @Autowired
    private AssetLifecycleService lifecycleService;

    @Autowired
    private AssetHistoryService historyService;

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

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_CREATE')")
    public ResponseEntity<AssetResponse> createAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = lifecycleService.createAsset(orgId, request, getPerformedBy(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    public ResponseEntity<List<AssetResponse>> getAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) AssetStatus status) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<AssetResponse> list = lifecycleService.getAssets(orgId, status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    public ResponseEntity<AssetResponse> getAssetById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = lifecycleService.getAssetById(orgId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    public ResponseEntity<AssetResponse> updateAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody EditAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = lifecycleService.updateAsset(orgId, id, request, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_DELETE')")
    public ResponseEntity<Void> deleteAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        lifecycleService.deleteAsset(orgId, id, getPerformedBy(user));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ASSET_ASSIGN')")
    public ResponseEntity<AssetActionResultResponse> assignAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody AssignAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = lifecycleService.assignAsset(orgId, id, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasAuthority('ASSET_TRANSFER')")
    public ResponseEntity<AssetActionResultResponse> transferAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody TransferAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = lifecycleService.transferAsset(orgId, id, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ASSET_RETURN')")
    public ResponseEntity<AssetResponse> returnAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody ReturnAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = lifecycleService.returnAsset(orgId, id, request, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAuthority('ASSET_RETIRE')")
    public ResponseEntity<AssetActionResultResponse> retireAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody RetireAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = lifecycleService.retireAsset(orgId, id, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/dispose")
    @PreAuthorize("hasAuthority('ASSET_DISPOSE')")
    public ResponseEntity<AssetActionResultResponse> disposeAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody DisposeAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = lifecycleService.disposeAsset(orgId, id, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('ASSET_HISTORY_VIEW')")
    public ResponseEntity<List<AssetHistoryResponse>> getAssetHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<AssetHistoryResponse> history = historyService.getAssetHistory(orgId, id);
        return ResponseEntity.ok(history);
    }
}
