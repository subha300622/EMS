package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.dto.OnboardingDocumentResponse;
import com.example.ems.onboarding.dto.OnboardingDocumentVerifyRequest;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingDocument;
import com.example.ems.onboarding.repository.OnboardingDocumentRepository;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.storage.service.FirebaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OnboardingDocumentService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingDocumentRepository onboardingDocumentRepository;

    @Autowired
    private FirebaseStorageService storageService;

    public List<OnboardingDocumentResponse> getDocuments(Long onboardingId) {
        onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));
        return onboardingDocumentRepository.findByOnboardingId(onboardingId).stream()
                .map(OnboardingDocumentResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OnboardingDocumentResponse uploadDocument(Long onboardingId, Long documentId, MultipartFile file, User user) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = onboardingDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding document not found with ID: " + documentId));

        if (!doc.getOnboarding().getId().equals(onboardingId)) {
            throw new IllegalArgumentException("Document does not belong to the specified onboarding profile");
        }

        // Access check: User must be employee undergoing onboarding, HR, or Admin
        Employee employee = onboarding.getEmployee();
        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        boolean isSelf = (user.getUserId() != null && user.getUserId().equals(employee.getEmployeeId()))
                || (user.getWorkEmail() != null && user.getWorkEmail().equalsIgnoreCase(employee.getEmail()));

        boolean isHR = "HR".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);

        if (!isSelf && !isHR && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to upload documents for this employee");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String targetFileName = UUID.randomUUID().toString() + extension;
            String folder = "onboarding/onb-" + onboardingId;

            String path = storageService.uploadFile(file, folder, targetFileName);

            doc.setFileName(originalName);
            doc.setFileType(file.getContentType());
            doc.setFileSize(file.getSize());
            doc.setFilePath(path);
            doc.setVerificationStatus("UPLOADED");
            doc.setUploadedAt(LocalDateTime.now());
            doc.setUploadedBy(user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId());
            doc.setDownloadUrl("/api/v1/onboarding/" + onboardingId + "/documents/doc-" + doc.getId() + "/download");

            OnboardingDocument saved = onboardingDocumentRepository.save(doc);
            return new OnboardingDocumentResponse(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document file: " + e.getMessage(), e);
        }
    }

    public InputStream downloadDocument(Long onboardingId, Long documentId, User user) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = onboardingDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding document not found with ID: " + documentId));

        if (!doc.getOnboarding().getId().equals(onboardingId)) {
            throw new IllegalArgumentException("Document does not belong to the specified onboarding profile");
        }

        if (doc.getFilePath() == null) {
            throw new ResourceNotFoundException("Document has not been uploaded yet");
        }

        // Access check: employee self, employee's manager, department HR, or Admins
        Employee employee = onboarding.getEmployee();
        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        boolean isSelf = (user.getUserId() != null && user.getUserId().equals(employee.getEmployeeId()))
                || (user.getWorkEmail() != null && user.getWorkEmail().equalsIgnoreCase(employee.getEmail()));

        boolean isManager = (onboarding.getManager() != null && user.getUserId() != null && user.getUserId().equals(onboarding.getManager().getEmployeeId()))
                || (employee.getManager() != null && user.getUserId() != null && user.getUserId().equals(employee.getManager().getEmployeeId()));

        boolean isHR = "HR".equalsIgnoreCase(role) && user.getDepartment() != null && user.getDepartment().equalsIgnoreCase(employee.getDepartment());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);

        if (!isSelf && !isManager && !isHR && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to download this document");
        }

        try {
            return storageService.downloadFileAsStream(doc.getFilePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from storage: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OnboardingDocumentResponse verifyDocument(Long onboardingId, Long documentId, OnboardingDocumentVerifyRequest request) {
        onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = onboardingDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding document not found with ID: " + documentId));

        if (!doc.getOnboarding().getId().equals(onboardingId)) {
            throw new IllegalArgumentException("Document does not belong to the specified onboarding profile");
        }

        if (doc.getFilePath() == null) {
            throw new ConflictException("Cannot verify a document that has not been uploaded yet");
        }

        String targetStatus = request.getStatus().toUpperCase();
        if (!"VERIFIED".equals(targetStatus) && !"REJECTED".equals(targetStatus)) {
            throw new IllegalArgumentException("Verification status must be either VERIFIED or REJECTED");
        }

        doc.setVerificationStatus(targetStatus);
        doc.setVerificationNotes(request.getRemarks());

        OnboardingDocument saved = onboardingDocumentRepository.save(doc);
        return new OnboardingDocumentResponse(saved);
    }

    @Transactional
    public void deleteDocument(Long onboardingId, Long documentId, User user) {
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        OnboardingDocument doc = onboardingDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding document not found with ID: " + documentId));

        if (!doc.getOnboarding().getId().equals(onboardingId)) {
            throw new IllegalArgumentException("Document does not belong to the specified onboarding profile");
        }

        // Access check: employee self, HR, or Admins
        Employee employee = onboarding.getEmployee();
        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        boolean isSelf = (user.getUserId() != null && user.getUserId().equals(employee.getEmployeeId()))
                || (user.getWorkEmail() != null && user.getWorkEmail().equalsIgnoreCase(employee.getEmail()));

        boolean isHR = "HR".equalsIgnoreCase(role);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);

        if (!isSelf && !isHR && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this document");
        }

        // Delete from storage if present
        if (doc.getFilePath() != null) {
            try {
                storageService.deleteFile(doc.getFilePath());
            } catch (Exception e) {
                // Log warning but continue cleaning database record
            }
        }

        // Reset document fields to pending state
        doc.setFileName("[Pending Upload] - " + doc.getDocumentType());
        doc.setFileType(null);
        doc.setFileSize(null);
        doc.setFilePath(null);
        doc.setVerificationStatus("PENDING");
        doc.setVerificationNotes(null);
        doc.setUploadedBy(null);

        onboardingDocumentRepository.save(doc);
    }
}
