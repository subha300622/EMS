package com.example.ems.asset.controller;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.asset.dto.AssetDtos.AssetActionResultResponse;
import com.example.ems.asset.entity.MyAssetRequest;
import com.example.ems.asset.service.AssetRequestService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets/requests")
@CrossOrigin("*")
@Tag(name = "Asset Requests", description = "Asset allocation request drafting, submission, and approval engine routing.")
public class AssetRequestController {

    @Autowired
    private AssetRequestService requestService;

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

    private String getPerformedBy(User user) {
        return user != null ? (user.getWorkEmail() != null ? user.getWorkEmail() : "User " + user.getId()) : "System User";
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "List Asset Requests", description = "Lists all asset allocation requests.")
    public ResponseEntity<List<MyAssetRequest>> getRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.getRequests(orgId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_REQUEST_CREATE') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Create Asset Request Draft", description = "Creates a draft asset request.")
    public ResponseEntity<MyAssetRequest> createRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String category,
            @RequestParam String model,
            @RequestParam String reason,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) LocalDate requiredByDate) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        Long empId = user.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.createRequest(orgId, empId, category, model, reason, priority, requiredByDate));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Request Details", description = "Retrieves details of a specific asset request.")
    public ResponseEntity<MyAssetRequest> getRequestById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.getRequestById(orgId, requestId));
    }

    @PutMapping("/{requestId}")
    @PreAuthorize("hasAuthority('ASSET_REQUEST_CREATE') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Update Request Draft", description = "Updates fields of a draft asset request.")
    public ResponseEntity<MyAssetRequest> updateRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) LocalDate requiredByDate) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.updateRequest(orgId, requestId, category, model, reason, priority, requiredByDate));
    }

    @PostMapping("/{requestId}/submit")
    @PreAuthorize("hasAuthority('ASSET_REQUEST_CREATE') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Submit Asset Request", description = "Submits asset request and initiates central approval engine workflow.")
    public ResponseEntity<AssetActionResultResponse> submitRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.submitRequest(orgId, requestId, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('ASSET_APPROVE')")
    @Operation(summary = "Approve Asset Request", description = "Delegates approval execution to central workflow engine.")
    public ResponseEntity<AssetActionResultResponse> approveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.approveRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('ASSET_REJECT')")
    @Operation(summary = "Reject Asset Request", description = "Delegates rejection execution to central workflow engine.")
    public ResponseEntity<AssetActionResultResponse> rejectRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.rejectRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/send-back")
    @PreAuthorize("hasAuthority('ASSET_APPROVE')")
    @Operation(summary = "Send Back Asset Request", description = "Requests revisions on an asset request.")
    public ResponseEntity<AssetActionResultResponse> sendBackRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.sendBackRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAuthority('ASSET_REQUEST_CREATE') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Cancel Asset Request", description = "Cancels an active asset request.")
    public ResponseEntity<AssetActionResultResponse> cancelRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(requestService.cancelRequest(orgId, requestId, getPerformedBy(user)));
    }
}
