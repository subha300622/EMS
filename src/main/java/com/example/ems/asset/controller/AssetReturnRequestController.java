package com.example.ems.asset.controller;

import com.example.ems.approval.dto.ApprovalActionRequest;
import com.example.ems.asset.dto.AssetDtos.AssetActionResultResponse;
import com.example.ems.asset.entity.MyAssetReturnRequest;
import com.example.ems.asset.service.AssetReturnRequestService;
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
@RequestMapping("/api/v1/assets/return-requests")
@CrossOrigin("*")
@Tag(name = "Asset Return Requests", description = "Asset return request management, submission, approval routing, and send-back.")
public class AssetReturnRequestController {

    @Autowired
    private AssetReturnRequestService returnRequestService;

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
    @Operation(summary = "List Return Requests", description = "Lists all asset return requests.")
    public ResponseEntity<List<MyAssetReturnRequest>> getReturnRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.getReturnRequests(orgId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_RETURN') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Create Return Request Draft", description = "Creates a draft asset return request.")
    public ResponseEntity<MyAssetReturnRequest> createReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long assetId,
            @RequestParam String returnReason,
            @RequestParam(required = false, defaultValue = "GOOD") String condition,
            @RequestParam(required = false) List<String> accessories,
            @RequestParam(required = false) String comments) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        Long empId = user.getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(returnRequestService.createReturnRequest(orgId, assetId, empId, returnReason, condition, accessories, comments));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Return Request Details", description = "Retrieves details of a specific return request.")
    public ResponseEntity<MyAssetReturnRequest> getReturnRequestById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.getReturnRequestById(orgId, requestId));
    }

    @PutMapping("/{requestId}")
    @PreAuthorize("hasAuthority('ASSET_RETURN') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Update Return Request Draft", description = "Updates fields of a draft return request.")
    public ResponseEntity<MyAssetReturnRequest> updateReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestParam(required = false) String returnReason,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) List<String> accessories,
            @RequestParam(required = false) String comments) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.updateReturnRequest(orgId, requestId, returnReason, condition, accessories, comments));
    }

    @PostMapping("/{requestId}/submit")
    @PreAuthorize("hasAuthority('ASSET_RETURN') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Submit Return Request", description = "Submits return request and routes to central approval engine.")
    public ResponseEntity<AssetActionResultResponse> submitReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.submitReturnRequest(orgId, requestId, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('ASSET_APPROVE')")
    @Operation(summary = "Approve Return Request", description = "Approves asset return request via central approval engine.")
    public ResponseEntity<AssetActionResultResponse> approveReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.approveReturnRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('ASSET_REJECT')")
    @Operation(summary = "Reject Return Request", description = "Rejects asset return request via central approval engine.")
    public ResponseEntity<AssetActionResultResponse> rejectReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.rejectReturnRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/send-back")
    @PreAuthorize("hasAuthority('ASSET_APPROVE')")
    @Operation(summary = "Send Back Return Request", description = "Requests revisions on a return request.")
    public ResponseEntity<AssetActionResultResponse> sendBackReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId,
            @RequestBody(required = false) ApprovalActionRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.sendBackReturnRequest(orgId, requestId, request, getPerformedBy(user)));
    }

    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAuthority('ASSET_RETURN') or hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Cancel Return Request", description = "Cancels an active return request.")
    public ResponseEntity<AssetActionResultResponse> cancelReturnRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long requestId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(returnRequestService.cancelReturnRequest(orgId, requestId, getPerformedBy(user)));
    }
}
