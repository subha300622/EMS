package com.example.ems.common.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.DmsDashboardResponse;
import com.example.ems.common.dto.DmsDocumentAuditLogResponse;
import com.example.ems.common.dto.DmsDocumentRequest;
import com.example.ems.common.dto.DmsDocumentResponse;
import com.example.ems.common.dto.DmsDocumentShareRequest;
import com.example.ems.common.dto.DmsDocumentShareResponse;
import com.example.ems.common.dto.DmsDocumentVersionRequest;
import com.example.ems.common.dto.DmsDocumentVersionResponse;
import com.example.ems.common.dto.DmsSignatureCompleteRequest;
import com.example.ems.common.dto.DmsSignatureRequest;
import com.example.ems.common.dto.DmsSignatureResponse;
import com.example.ems.common.entity.DmsDocument;
import com.example.ems.common.entity.DmsDocumentAuditLog;
import com.example.ems.common.entity.DmsDocumentShare;
import com.example.ems.common.entity.DmsDocumentSignature;
import com.example.ems.common.entity.DmsDocumentVersion;
import com.example.ems.common.repository.DmsDocumentAuditLogRepository;
import com.example.ems.common.repository.DmsDocumentRepository;
import com.example.ems.common.repository.DmsDocumentShareRepository;
import com.example.ems.common.repository.DmsDocumentSignatureRepository;
import com.example.ems.common.repository.DmsDocumentVersionRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DmsService {

    @Autowired private DmsDocumentRepository documentRepository;
    @Autowired private DmsDocumentVersionRepository versionRepository;
    @Autowired private DmsDocumentShareRepository shareRepository;
    @Autowired private DmsDocumentAuditLogRepository auditLogRepository;
    @Autowired private DmsDocumentSignatureRepository signatureRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;

    // ── 1. DASHBOARD ─────────────────────────────────────────────────────────
    @Cacheable(value = "dmsDashboard", key = "'stats'")
    public DmsDashboardResponse getDashboardStats() {
        DmsDashboardResponse stats = new DmsDashboardResponse();

        long totalDocs = documentRepository.count();
        long pending = documentRepository.findByStatus("PENDING").size();
        long approved = documentRepository.findByStatus("APPROVED").size();
        long rejected = documentRepository.findByStatus("REJECTED").size();

        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusDays(30);
        long expiring = documentRepository.findByExpiryDateBetween(today, nextMonth).size();

        long shares = shareRepository.count();
        long sigRequests = signatureRepository.count();
        long pendingSig = signatureRepository.findByStatus("PENDING").size();

        stats.setTotalDocuments(totalDocs);
        stats.setPendingApprovals(pending);
        stats.setApprovedDocuments(approved);
        stats.setRejectedDocuments(rejected);
        stats.setExpiringSoon(expiring);
        stats.setTotalShares(shares);
        stats.setTotalSignatureRequests(sigRequests);
        stats.setPendingSignatureRequests(pendingSig);

        return stats;
    }

    // ── 2. CREATE DOCUMENT ───────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsDocumentResponse createDocument(DmsDocumentRequest request, String uploadedByEmail) {
        Employee owner = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee owner not found with ID: " + request.getEmployeeId()));

        User uploader = userRepository.findByWorkEmail(uploadedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Uploading user not found with email: " + uploadedByEmail));

        DmsDocument doc = new DmsDocument();
        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setCategory(request.getCategory().toUpperCase());
        doc.setExpiryDate(request.getExpiryDate());
        doc.setFileName(request.getFileName());
        doc.setFileType(request.getFileType());
        doc.setFileSize(request.getFileSize());
        doc.setDownloadUrl(request.getDownloadUrl());
        doc.setOwner(owner);
        doc.setUploadedBy(uploader);
        doc.setStatus("PENDING");
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        DmsDocument savedDoc = documentRepository.save(doc);

        // Auto-provision Version 1
        DmsDocumentVersion version = new DmsDocumentVersion();
        version.setDocument(savedDoc);
        version.setVersionNumber(1);
        version.setFileName(request.getFileName());
        version.setFileSize(request.getFileSize());
        version.setDownloadUrl(request.getDownloadUrl());
        version.setUploadedBy(uploader);
        version.setUploadedAt(LocalDateTime.now());
        version.setChangeNotes("Initial upload");
        versionRepository.save(version);

        // Log audit trail
        logAuditTrail(savedDoc, "UPLOADED", uploader, "Initial version 1 uploaded");

        return new DmsDocumentResponse(savedDoc);
    }

    public List<DmsDocumentResponse> getDocuments() {
        return documentRepository.findAll().stream()
                .map(DmsDocumentResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<DmsDocumentResponse> getDocumentById(Long id) {
        return documentRepository.findById(id).map(DmsDocumentResponse::new);
    }

    public List<DmsDocumentResponse> getDocumentsByOwner(Long ownerId) {
        return documentRepository.findByOwnerId(ownerId).stream()
                .map(DmsDocumentResponse::new)
                .collect(Collectors.toList());
    }

    // ── 3. GET MY DOCUMENTS (OWN + SHARED WITH ME) ───────────────────────────
    public List<DmsDocumentResponse> getMyDocuments(String employeeEmail) {
        Employee emp = employeeRepository.findByEmail(employeeEmail).orElse(null);
        if (emp == null) return List.of();

        // Documents owned by employee
        List<DmsDocument> ownedDocs = documentRepository.findByOwnerId(emp.getId());

        // Documents shared with employee
        List<DmsDocument> sharedDocs = shareRepository.findBySharedWithId(emp.getId()).stream()
                .map(DmsDocumentShare::getDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Combine unique documents
        Set<DmsDocument> allDocs = new LinkedHashSet<>(ownedDocs);
        allDocs.addAll(sharedDocs);

        return allDocs.stream().map(DmsDocumentResponse::new).collect(Collectors.toList());
    }

    // ── 4. DOWNLOAD DOCUMENT (AUDITED) ───────────────────────────────────────
    @Transactional
    public Optional<DmsDocumentResponse> downloadDocument(Long id, String performedByEmail) {
        return documentRepository.findById(id).map(doc -> {
            User user = userRepository.findByWorkEmail(performedByEmail).orElse(null);
            if (user != null) {
                logAuditTrail(doc, "DOWNLOADED", user, "Document file downloaded");
            }
            return new DmsDocumentResponse(doc);
        });
    }

    // ── 5. APPROVAL / REJECTION ──────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public Optional<DmsDocumentResponse> approveDocument(Long id, String performedByEmail) {
        return documentRepository.findById(id).map(doc -> {
            User user = userRepository.findByWorkEmail(performedByEmail).orElse(null);
            doc.setStatus("APPROVED");
            doc.setUpdatedAt(LocalDateTime.now());
            DmsDocument saved = documentRepository.save(doc);
            if (user != null) {
                logAuditTrail(saved, "APPROVED", user, "Document approved by HR manager");
            }
            return new DmsDocumentResponse(saved);
        });
    }

    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public Optional<DmsDocumentResponse> rejectDocument(Long id, String performedByEmail) {
        return documentRepository.findById(id).map(doc -> {
            User user = userRepository.findByWorkEmail(performedByEmail).orElse(null);
            doc.setStatus("REJECTED");
            doc.setUpdatedAt(LocalDateTime.now());
            DmsDocument saved = documentRepository.save(doc);
            if (user != null) {
                logAuditTrail(saved, "REJECTED", user, "Document rejected by HR manager");
            }
            return new DmsDocumentResponse(saved);
        });
    }

    // ── 6. VERSIONS ──────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsDocumentVersionResponse addVersion(Long documentId, DmsDocumentVersionRequest request, String uploadedByEmail) {
        DmsDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        User user = userRepository.findByWorkEmail(uploadedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + uploadedByEmail));

        // Determine next version number
        List<DmsDocumentVersion> versions = versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
        int nextVersionNum = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;

        DmsDocumentVersion version = new DmsDocumentVersion();
        version.setDocument(doc);
        version.setVersionNumber(nextVersionNum);
        version.setFileName(request.getFileName());
        version.setFileSize(request.getFileSize());
        version.setDownloadUrl(request.getDownloadUrl());
        version.setUploadedBy(user);
        version.setUploadedAt(LocalDateTime.now());
        version.setChangeNotes(request.getChangeNotes() != null ? request.getChangeNotes() : "Version " + nextVersionNum);

        DmsDocumentVersion savedVersion = versionRepository.save(version);

        // Update root document metadata
        doc.setFileName(request.getFileName());
        doc.setFileSize(request.getFileSize());
        doc.setDownloadUrl(request.getDownloadUrl());
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        // Log audit trail
        logAuditTrail(doc, "VERSION_ADDED", user, "New version " + nextVersionNum + " uploaded");

        return new DmsDocumentVersionResponse(savedVersion);
    }

    public List<DmsDocumentVersionResponse> getVersions(Long documentId) {
        return versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream()
                .map(DmsDocumentVersionResponse::new)
                .collect(Collectors.toList());
    }

    // ── 7. SHARE DOCUMENT ────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsDocumentShareResponse shareDocument(Long documentId, DmsDocumentShareRequest request, String sharedByEmail) {
        DmsDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        Employee sharedWith = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee recipient not found with ID: " + request.getEmployeeId()));

        User user = userRepository.findByWorkEmail(sharedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sharing user not found with email: " + sharedByEmail));

        // Check if already shared
        List<DmsDocumentShare> existing = shareRepository.findByDocumentId(documentId);
        Optional<DmsDocumentShare> match = existing.stream()
                .filter(s -> s.getSharedWith().getId().equals(sharedWith.getId()))
                .findFirst();

        DmsDocumentShare share = match.orElse(new DmsDocumentShare());
        share.setDocument(doc);
        share.setSharedWith(sharedWith);
        share.setSharedBy(user);
        share.setSharedAt(LocalDateTime.now());
        share.setAccessLevel(request.getAccessLevel() != null ? request.getAccessLevel().toUpperCase() : "READ");

        DmsDocumentShare saved = shareRepository.save(share);

        logAuditTrail(doc, "SHARED", user, "Document shared with " + sharedWith.getFullName());

        return new DmsDocumentShareResponse(saved);
    }

    public List<DmsDocumentShareResponse> getSharesByDocument(Long documentId) {
        return shareRepository.findByDocumentId(documentId).stream()
                .map(DmsDocumentShareResponse::new)
                .collect(Collectors.toList());
    }

    // ── 8. AUDIT LOGS ────────────────────────────────────────────────────────
    public List<DmsDocumentAuditLogResponse> getAuditLogs(Long documentId) {
        return auditLogRepository.findByDocumentIdOrderByPerformedAtDesc(documentId).stream()
                .map(DmsDocumentAuditLogResponse::new)
                .collect(Collectors.toList());
    }

    // ── 9. EXPIRING DOCUMENTS ────────────────────────────────────────────────
    public List<DmsDocumentResponse> getExpiringDocuments() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        return documentRepository.findByExpiryDateBetween(start, end).stream()
                .map(DmsDocumentResponse::new)
                .collect(Collectors.toList());
    }

    public List<DmsDocumentResponse> getExpiringDocumentsByEmployee(Long employeeId) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);
        return documentRepository.findByExpiryDateBetween(start, end).stream()
                .filter(doc -> doc.getOwner() != null && doc.getOwner().getId().equals(employeeId))
                .map(DmsDocumentResponse::new)
                .collect(Collectors.toList());
    }

    // ── 10. SIGNATURE REQUESTS ───────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsSignatureResponse submitSignatureRequest(Long documentId, DmsSignatureRequest request, String requestedByEmail) {
        DmsDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        Employee requestedFrom = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee signer not found with ID: " + request.getEmployeeId()));

        User user = userRepository.findByWorkEmail(requestedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requesting user not found with email: " + requestedByEmail));

        // Create new signature request record
        DmsDocumentSignature sig = new DmsDocumentSignature();
        sig.setDocument(doc);
        sig.setRequestedFrom(requestedFrom);
        sig.setRequestedBy(user);
        sig.setStatus("PENDING");
        sig.setComments(request.getComments());

        DmsDocumentSignature saved = signatureRepository.save(sig);

        logAuditTrail(doc, "SIGNATURE_REQUESTED", user, "Signature requested from " + requestedFrom.getFullName());

        return new DmsSignatureResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsSignatureResponse completeSignature(Long documentId, DmsSignatureCompleteRequest request, String signerEmail) {
        Employee emp = employeeRepository.findByEmail(signerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employee signer record not found for email: " + signerEmail));

        List<DmsDocumentSignature> signatures = signatureRepository.findByDocumentId(documentId);
        DmsDocumentSignature sig = signatures.stream()
                .filter(s -> s.getRequestedFrom().getId().equals(emp.getId()) && "PENDING".equals(s.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pending signature request not found for document ID: " + documentId + " and employee email: " + signerEmail));

        User user = userRepository.findByWorkEmail(signerEmail).orElse(null);
        if (user == null) {
            // Fallback user if signer does not have user record (e.g. employee signature only)
            user = sig.getRequestedBy();
        }

        String action = request.getStatus().toUpperCase();
        if (!"SIGNED".equals(action) && !"DECLINED".equals(action)) {
            throw new IllegalArgumentException("Invalid signature action status: " + request.getStatus());
        }

        sig.setStatus(action);
        sig.setSignedAt(LocalDateTime.now());
        sig.setSignatureDate(LocalDate.now());
        sig.setComments(request.getComments());
        DmsDocumentSignature saved = signatureRepository.save(sig);

        logAuditTrail(sig.getDocument(), action, user, "Document " + action.toLowerCase() + " by signer: " + emp.getFullName() + ". Comments: " + request.getComments());

        return new DmsSignatureResponse(saved);
    }

    public List<DmsSignatureResponse> getSignaturesByDocument(Long documentId) {
        return signatureRepository.findByDocumentId(documentId).stream()
                .map(DmsSignatureResponse::new)
                .collect(Collectors.toList());
    }

    // ── 11. REPORTS ──────────────────────────────────────────────────────────
    public Map<String, Object> getReports(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        long totalDocs = documentRepository.count();
        long approved = documentRepository.findByStatus("APPROVED").size();
        long rejected = documentRepository.findByStatus("REJECTED").size();
        long pending = documentRepository.findByStatus("PENDING").size();

        data.put("totalDocumentsCount", totalDocs);
        data.put("approvedCount", approved);
        data.put("rejectedCount", rejected);
        data.put("pendingCount", pending);
        data.put("approvalRate", totalDocs > 0 ? Math.round(((double) approved / totalDocs) * 100.0 * 100.0) / 100.0 : 0.0);

        // Category breakdown
        Map<String, Long> categoryStats = documentRepository.findAll().stream()
                .collect(Collectors.groupingBy(DmsDocument::getCategory, Collectors.counting()));
        data.put("categoryBreakdown", categoryStats);

        // Signature breakdown
        long totalSigs = signatureRepository.count();
        long signed = signatureRepository.findByStatus("SIGNED").size();
        long pendingSigs = signatureRepository.findByStatus("PENDING").size();
        long declined = signatureRepository.findByStatus("DECLINED").size();
        data.put("totalSignatureRequests", totalSigs);
        data.put("signedCount", signed);
        data.put("pendingSignatureCount", pendingSigs);
        data.put("declinedCount", declined);

        return data;
    }

    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public DmsDocumentResponse updateDocument(Long id, DmsDocumentRequest request, String performedByEmail) {
        DmsDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        User user = userRepository.findByWorkEmail(performedByEmail).orElse(null);

        doc.setTitle(request.getTitle());
        doc.setDescription(request.getDescription());
        doc.setCategory(request.getCategory().toUpperCase());
        doc.setExpiryDate(request.getExpiryDate());
        if (request.getFileName() != null && !request.getFileName().isBlank()) {
            doc.setFileName(request.getFileName());
        }
        if (request.getFileType() != null && !request.getFileType().isBlank()) {
            doc.setFileType(request.getFileType());
        }
        if (request.getFileSize() != null) {
            doc.setFileSize(request.getFileSize());
        }
        if (request.getDownloadUrl() != null && !request.getDownloadUrl().isBlank()) {
            doc.setDownloadUrl(request.getDownloadUrl());
        }
        doc.setUpdatedAt(LocalDateTime.now());

        DmsDocument saved = documentRepository.save(doc);

        if (user != null) {
            logAuditTrail(saved, "UPDATED", user, "Document details updated");
        }

        return new DmsDocumentResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "dmsDashboard", allEntries = true)
    public boolean deleteDocument(Long id, String performedByEmail) {
        if (documentRepository.existsById(id)) {
            // First delete all audit log records referencing this document
            auditLogRepository.findByDocumentIdOrderByPerformedAtDesc(id).forEach(log -> auditLogRepository.delete(log));

            // Delete all shares referencing this document
            shareRepository.findByDocumentId(id).forEach(s -> shareRepository.delete(s));

            // Delete all signature requests referencing this document
            signatureRepository.findByDocumentId(id).forEach(sig -> signatureRepository.delete(sig));

            // Delete all versions referencing this document
            versionRepository.findByDocumentIdOrderByVersionNumberDesc(id).forEach(v -> versionRepository.delete(v));

            // Finally delete the document
            documentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private void logAuditTrail(DmsDocument doc, String action, User user, String details) {
        DmsDocumentAuditLog log = new DmsDocumentAuditLog();
        log.setDocument(doc);
        log.setAction(action.toUpperCase());
        log.setPerformedBy(user);
        log.setPerformedAt(LocalDateTime.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
