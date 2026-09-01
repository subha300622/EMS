package com.example.ems.asset.controller;

import com.example.ems.asset.entity.AssetAssignment;
import com.example.ems.asset.entity.AssetTransfer;
import com.example.ems.asset.service.AssetAssignmentService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Asset Assignments & Transfers", description = "Immutable first-class assignment and ownership transfer records.")
public class AssetAssignmentController {

    @Autowired
    private AssetAssignmentService assignmentService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                if (email != null) {
                    User u = userRepository.findByWorkEmail(email).orElseGet(() -> userRepository.findByUserId(email).orElse(null));
                    if (u != null) return u;
                }
            }
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof com.example.ems.security.dto.AuthPrincipal p) {
                if (p.getEmail() != null) {
                    User u = userRepository.findByWorkEmail(p.getEmail()).orElseGet(() -> userRepository.findByUserId(p.getEmail()).orElse(null));
                    if (u != null) return u;
                }
                if (p.getUserId() != null) {
                    User u = userRepository.findByUserId(p.getUserId()).orElseGet(() -> userRepository.findByWorkEmail(p.getUserId()).orElse(null));
                    if (u != null) return u;
                }
            }
            String principal = auth.getName();
            if (principal != null && !principal.isBlank()) {
                return userRepository.findByUserId(principal)
                        .orElseGet(() -> userRepository.findByWorkEmail(principal).orElse(null));
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

    @GetMapping("/assignments")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "List All Assignments", description = "Retrieves all assignment records across the organization.")
    public ResponseEntity<List<AssetAssignment>> getAssignments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(assignmentService.getAssignments(orgId));
    }

    @GetMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Assignment Details", description = "Retrieves details of a specific assignment record.")
    public ResponseEntity<AssetAssignment> getAssignmentById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assignmentId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(assignmentService.getAssignmentById(orgId, assignmentId));
    }

    @GetMapping("/{assetId}/assignments")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Asset Assignment History", description = "Lists assignment history records for a specific asset.")
    public ResponseEntity<List<AssetAssignment>> getAssetAssignments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(assignmentService.getAssetAssignments(orgId, assetId));
    }

    @GetMapping("/{assetId}/transfers")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Asset Transfer History", description = "Lists ownership transfer records for a specific asset.")
    public ResponseEntity<List<AssetTransfer>> getAssetTransfers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(assignmentService.getAssetTransfers(orgId, assetId));
    }
}
