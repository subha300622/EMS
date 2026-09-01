package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.AssetDocument;
import com.example.ems.asset.entity.AssetStatus;
import com.example.ems.asset.service.*;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@CrossOrigin("*")
@Tag(name = "Asset Management", description = "Organization hardware assets assignments, return logs, value depreciation.")
public class AssetManagementController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetLifecycleService lifecycleService;

    @Autowired
    private AssetHistoryService historyService;

    @Autowired
    private AssetDocumentService documentService;

    @Autowired
    private AssetQrCodeService qrCodeService;

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
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
        if (user != null) {
            return user.getWorkEmail() != null ? user.getWorkEmail() : "User " + user.getId();
        }
        return "System User";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Group 1: Asset Master Operations
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_CREATE')")
    @Operation(summary = "Create Asset", description = "Creates a new asset master record in DRAFT status.")
    public ResponseEntity<AssetResponse> createAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = assetService.createAsset(orgId, request, getPerformedBy(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "List & Filter Assets", description = "Paginated listing with search, category, status, and sorting filters.")
    public ResponseEntity<PaginatedAssetResponse> getAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        PaginatedAssetResponse response = assetService.getAssets(orgId, status, categoryId, search, sortField, sortDir, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Asset Details", description = "Retrieves full details for a specific asset.")
    public ResponseEntity<AssetResponse> getAssetById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = assetService.getAssetById(orgId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Update Asset Master", description = "Updates non-status asset fields. Status mutation via PUT is strictly prohibited.")
    public ResponseEntity<AssetResponse> updateAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody EditAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = assetService.updateAsset(orgId, id, request, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_DELETE')")
    @Operation(summary = "Soft-Delete Asset", description = "Soft-deletes asset record while preserving historical assignment/audit history.")
    public ResponseEntity<Void> deleteAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        assetService.deleteAsset(orgId, id, getPerformedBy(user));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('ASSET_HISTORY_VIEW')")
    @Operation(summary = "Get Asset Audit History", description = "Retrieves complete immutable event audit trail for an asset.")
    public ResponseEntity<List<AssetHistoryResponse>> getAssetHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<AssetHistoryResponse> history = historyService.getAssetHistory(orgId, id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Asset Documents", description = "List all document attachments associated with an asset.")
    public ResponseEntity<List<AssetDocumentResponse>> getAssetDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(documentService.getAssetDocuments(orgId, id));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Upload Asset Document", description = "Uploads a document attachment (invoice, warranty card, contract) for an asset.")
    public ResponseEntity<AssetDocumentResponse> uploadAssetDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(value = "documentType", defaultValue = "GENERAL") String documentType,
            @RequestPart("file") MultipartFile file) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetDocumentResponse response = documentService.uploadAssetDocument(orgId, id, documentType, file, getPerformedBy(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Download Asset Document", description = "Downloads raw document byte stream.")
    public ResponseEntity<byte[]> downloadAssetDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long documentId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetDocument doc = documentService.getAssetDocumentEntity(orgId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(doc.getFileData());
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @PreAuthorize("hasAuthority('ASSET_DELETE')")
    @Operation(summary = "Delete Asset Document", description = "Removes a document attachment from an asset.")
    public ResponseEntity<Void> deleteAssetDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @PathVariable Long documentId) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        documentService.deleteAssetDocument(orgId, id, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qr-code")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get Asset QR Code Image", description = "Generates a dynamic PNG QR code encoding the public verification URL.")
    public ResponseEntity<byte[]> getAssetQrCode(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        byte[] qrImage = qrCodeService.generateAssetQrCodePng(orgId, id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @GetMapping("/verify/{assetCode}")
    @Operation(summary = "Public Asset Verification", description = "Public verification endpoint returning safe verification status by asset code.")
    public ResponseEntity<AssetVerificationResponse> verifyAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(assetService.verifyAsset(assetCode));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Group 2: Asset Lifecycle State Transitions
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Activate Asset", description = "Transitions asset from DRAFT to ACTIVE status.")
    public ResponseEntity<AssetResponse> activateAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.activateAsset(orgId, id, getPerformedBy(user)));
    }

    @PostMapping("/{id}/make-available")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Make Asset Available", description = "Restocks/inspects asset from ACTIVE, RETURNED, REPAIR, or IN_MAINTENANCE to AVAILABLE status.")
    public ResponseEntity<AssetResponse> makeAvailableAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.makeAvailableAsset(orgId, id, getPerformedBy(user)));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ASSET_ASSIGN')")
    @Operation(summary = "Assign Asset", description = "Assigns an AVAILABLE asset to an employee.")
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
    @Operation(summary = "Transfer Asset", description = "Atomically transfers an ASSIGNED asset to another employee/location.")
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
    @Operation(summary = "Return Asset", description = "Returns an ASSIGNED asset to RETURNED status.")
    public ResponseEntity<AssetResponse> returnAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody ReturnAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetResponse response = lifecycleService.returnAsset(orgId, id, request, getPerformedBy(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/mark-lost")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Mark Asset Lost", description = "Transitions ASSIGNED asset to LOST status.")
    public ResponseEntity<AssetResponse> markLostAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.markLostAsset(orgId, id, remarks, getPerformedBy(user)));
    }

    @PostMapping("/{id}/mark-damaged")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Mark Asset Damaged", description = "Transitions ASSIGNED asset to DAMAGED status.")
    public ResponseEntity<AssetResponse> markDamagedAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.markDamagedAsset(orgId, id, remarks, getPerformedBy(user)));
    }

    @PostMapping("/{id}/repair")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Start Repair", description = "Transitions LOST or DAMAGED asset to REPAIR status.")
    public ResponseEntity<AssetResponse> startRepair(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.startRepairAsset(orgId, id, remarks, getPerformedBy(user)));
    }

    @PostMapping("/{id}/maintenance")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Start Maintenance", description = "Transitions AVAILABLE, DAMAGED, or REPAIR asset to IN_MAINTENANCE status.")
    public ResponseEntity<AssetResponse> startMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.startMaintenanceAsset(orgId, id, remarks, getPerformedBy(user)));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Restore Asset", description = "Restores LOST, DAMAGED, REPAIR, or IN_MAINTENANCE asset back to AVAILABLE.")
    public ResponseEntity<AssetResponse> restoreAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        return ResponseEntity.ok(lifecycleService.restoreAsset(orgId, id, remarks, getPerformedBy(user)));
    }

    @PostMapping("/{id}/retire")
    @PreAuthorize("hasAuthority('ASSET_RETIRE')")
    @Operation(summary = "Retire Asset", description = "Transitions AVAILABLE, REPAIR, or IN_MAINTENANCE asset to RETIRED status.")
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
    @Operation(summary = "Dispose Asset", description = "Transitions RETIRED asset to terminal DISPOSED status.")
    public ResponseEntity<AssetActionResultResponse> disposeAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody DisposeAssetRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        AssetActionResultResponse response = lifecycleService.disposeAsset(orgId, id, request, user.getId(), getPerformedBy(user));
        return ResponseEntity.ok(response);
    }
}
