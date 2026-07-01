package com.example.ems.employee.service;

import com.example.ems.employee.dto.*;
import com.example.ems.employee.entity.*;
import com.example.ems.employee.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.auth.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyDocumentService {

    @Autowired
    private MyDocumentCategoryRepository categoryRepository;

    @Autowired
    private MyDocumentTypeRepository typeRepository;

    @Autowired
    private MyEmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private MyDocumentActivityRepository activityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleService roleService;

    private boolean isAuthorizedToAccess(MyEmployeeDocument doc, String authEmail) {
        if (doc.getEmployee().getEmail().equalsIgnoreCase(authEmail)) {
            return true;
        }
        return roleService.hasPermission(authEmail, "document.employee.manage")
                || roleService.hasPermission(authEmail, "document.employee.read")
                || roleService.isSuperAdmin(authEmail);
    }

    @Transactional
    public void seedCoreCategoriesAndTypes() {
        if (categoryRepository.count() > 0) {
            return;
        }
        // Categories
        MyDocumentCategory identity = categoryRepository.save(new MyDocumentCategory("Identity Documents", "IDENTITY"));
        MyDocumentCategory employment = categoryRepository.save(new MyDocumentCategory("Employment Documents", "EMPLOYMENT"));
        MyDocumentCategory education = categoryRepository.save(new MyDocumentCategory("Educational Certificates", "EDUCATION"));
        MyDocumentCategory financial = categoryRepository.save(new MyDocumentCategory("Financial Documents", "FINANCIAL"));
        MyDocumentCategory health = categoryRepository.save(new MyDocumentCategory("Health & Insurance", "HEALTH"));
        MyDocumentCategory company = categoryRepository.save(new MyDocumentCategory("Company-Issued Documents", "COMPANY"));

        // Document Types
        // Identity
        typeRepository.save(new MyDocumentType("AADHAR_CARD", "Aadhar Card", identity, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("PAN_CARD", "PAN Card", identity, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("PASSPORT", "Passport", identity, true, true, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("DRIVING_LICENSE", "Driving License", identity, false, true, List.of("PDF", "JPG", "PNG"), 10));

        // Employment
        typeRepository.save(new MyDocumentType("OFFER_LETTER", "Offer Letter", employment, true, false, List.of("PDF"), 15));
        typeRepository.save(new MyDocumentType("APPOINTMENT_LETTER", "Appointment Letter", employment, true, false, List.of("PDF"), 15));
        typeRepository.save(new MyDocumentType("NDA_EMPLOYMENT", "NDA", employment, true, false, List.of("PDF"), 15));
        typeRepository.save(new MyDocumentType("LAST_APPRAISAL", "Last Appraisal", employment, false, false, List.of("PDF"), 15));

        // Education
        typeRepository.save(new MyDocumentType("CERTIFICATE_10TH", "10th Certificate", education, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("CERTIFICATE_12TH", "12th Certificate", education, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("DEGREE_BTECH", "B.Tech Degree", education, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("PG_DIPLOMA", "PG Diploma", education, false, false, List.of("PDF", "JPG", "PNG"), 10));

        // Financial
        typeRepository.save(new MyDocumentType("BANK_PASSBOOK", "Bank Passbook", financial, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("FORM_16", "Form 16 (2024-25)", financial, true, false, List.of("PDF"), 15));
        typeRepository.save(new MyDocumentType("INVESTMENT_PROOFS", "Investment Proofs", financial, false, false, List.of("PDF", "ZIP"), 20));

        // Health
        typeRepository.save(new MyDocumentType("MEDICAL_INSURANCE", "Medical Insurance Card", health, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("HEALTH_DECLARATION", "Health Declaration", health, true, false, List.of("PDF", "JPG", "PNG"), 10));

        // Company
        typeRepository.save(new MyDocumentType("COMPANY_ID", "ID Card (Digital)", company, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("ACCESS_BADGE", "Access Badge", company, true, false, List.of("PDF", "JPG", "PNG"), 10));
        typeRepository.save(new MyDocumentType("COMPANY_NDA", "NDA", company, true, false, List.of("PDF"), 15));
    }

    @Transactional
    public void seedMockEmployeeDocuments() {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            seedMockDocumentsForEmployee(emp);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void seedMockDocumentsForEmployee(Employee emp) {
        if (employeeDocumentRepository.findByEmployeeId(emp.getId()).size() > 0) {
            return;
        }

        byte[] dummyPdf = "Mock PDF Content Stream %PDF-1.4".getBytes(StandardCharsets.UTF_8);

        // Map of document types to seed
        Map<String, String> fileNames = Map.ofEntries(
            Map.entry("AADHAR_CARD", "aadhar_card.pdf"),
            Map.entry("PAN_CARD", "pan_card.pdf"),
            Map.entry("PASSPORT", "passport.pdf"),
            Map.entry("OFFER_LETTER", "offer_letter.pdf"),
            Map.entry("APPOINTMENT_LETTER", "appointment_letter.pdf"),
            Map.entry("NDA_EMPLOYMENT", "nda.pdf"),
            Map.entry("LAST_APPRAISAL", "last_appraisal.pdf"),
            Map.entry("CERTIFICATE_10TH", "10th_certificate.pdf"),
            Map.entry("CERTIFICATE_12TH", "12th_certificate.pdf"),
            Map.entry("DEGREE_BTECH", "btech_degree.pdf"),
            Map.entry("BANK_PASSBOOK", "bank_passbook.pdf"),
            Map.entry("FORM_16", "form_16.pdf"),
            Map.entry("MEDICAL_INSURANCE", "medical_insurance.pdf"),
            Map.entry("HEALTH_DECLARATION", "health_declaration.pdf"),
            Map.entry("COMPANY_ID", "id_card.pdf"),
            Map.entry("ACCESS_BADGE", "access_badge.pdf"),
            Map.entry("COMPANY_NDA", "company_nda.pdf")
        );

        for (Map.Entry<String, String> entry : fileNames.entrySet()) {
            typeRepository.findByCode(entry.getKey()).ifPresent(type -> {
                MyEmployeeDocument doc = new MyEmployeeDocument();
                doc.setEmployee(emp);
                doc.setDocumentType(type);
                doc.setFileName(entry.getValue());
                doc.setFileType("application/pdf");
                doc.setFileData(dummyPdf);
                doc.setFileSize("1.5 MB");
                doc.setVersion(1);
                doc.setStatus("UPLOADED");
                doc.setVerificationStatus("APPROVED");
                doc.setVerifiedBy("HR Manager");
                doc.setVerifiedAt(LocalDateTime.now().minusDays(5));
                doc.setVerificationRemarks("Verified successfully");
                doc.setCreatedAt(LocalDateTime.now().minusMonths(2));
                doc.setUpdatedAt(LocalDateTime.now().minusMonths(2));

                if ("PASSPORT".equals(type.getCode())) {
                    // Seed passport to expire in 30 days from test local time (2026-06-16 -> 2026-07-16)
                    doc.setExpiryDate(LocalDate.of(2026, 7, 16));
                    doc.setDocumentNumber("P1234567");
                    doc.setIssuedDate(LocalDate.of(2016, 7, 16));
                }

                employeeDocumentRepository.save(doc);

                // Add activity log
                activityRepository.save(new MyDocumentActivity(
                    emp,
                    "DOCUMENT_UPLOADED",
                    type.getName(),
                    emp.getFullName(),
                    LocalDateTime.now().minusMonths(2)
                ));

                activityRepository.save(new MyDocumentActivity(
                    emp,
                    "DOCUMENT_APPROVED",
                    type.getName(),
                    "HR Manager",
                    LocalDateTime.now().minusDays(5)
                ));
            });
        }
    }

    private String getDocumentStatus(MyEmployeeDocument doc) {
        if (doc.getExpiryDate() != null) {
            LocalDate today = LocalDate.now();
            if (doc.getExpiryDate().isBefore(today)) {
                return "EXPIRED";
            } else if (doc.getExpiryDate().isBefore(today.plusDays(31))) {
                return "EXPIRING_SOON";
            }
        }
        return doc.getStatus();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Transactional
    public MyDocumentsDashboardResponse getDocumentDashboard(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        // Auto-seed mock documents if not exists (for instance when new users login)
        seedMockDocumentsForEmployee(emp);

        List<MyDocumentType> allTypes = typeRepository.findAll();
        List<MyEmployeeDocument> allDocs = employeeDocumentRepository.findByEmployeeId(emp.getId());

        int totalDocuments = allTypes.size();
        int uploadedDocuments = allDocs.size();

        long pendingDocuments = allTypes.stream()
                .filter(MyDocumentType::isMandatory)
                .filter(type -> allDocs.stream().noneMatch(d -> d.getDocumentType().getId().equals(type.getId()) && "APPROVED".equals(d.getVerificationStatus())))
                .count();

        long expiringSoon = allDocs.stream()
                .filter(d -> "EXPIRING_SOON".equals(getDocumentStatus(d)))
                .count();

        int completionPercentage = totalDocuments > 0 ? (uploadedDocuments * 100) / totalDocuments : 0;

        MyDocumentsDashboardResponse.EmployeeInfo empInfo = new MyDocumentsDashboardResponse.EmployeeInfo(
                emp.getId(), emp.getEmployeeId(), emp.getFullName()
        );

        MyDocumentsDashboardResponse.SummaryInfo summary = new MyDocumentsDashboardResponse.SummaryInfo(
                totalDocuments, uploadedDocuments, (int) pendingDocuments, (int) expiringSoon, completionPercentage
        );

        List<MyDocumentsDashboardResponse.AlertInfo> alerts = new ArrayList<>();
        if (pendingDocuments > 0) {
            alerts.add(new MyDocumentsDashboardResponse.AlertInfo(
                    "PENDING_UPLOAD",
                    pendingDocuments + " documents pending upload",
                    (int) pendingDocuments
            ));
        }

        allDocs.stream()
                .filter(d -> "EXPIRING_SOON".equals(getDocumentStatus(d)))
                .forEach(d -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), d.getExpiryDate());
                    alerts.add(new MyDocumentsDashboardResponse.AlertInfo(
                            "EXPIRING_SOON",
                            d.getDocumentType().getName() + " expires in " + days + " days",
                            d.getId(),
                            d.getExpiryDate(),
                            "WARNING"
                    ));
                });

        return new MyDocumentsDashboardResponse(empInfo, summary, alerts);
    }

    @Transactional
    public MyDocumentCategoriesResponse getDocumentCategories(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        seedMockDocumentsForEmployee(emp);

        List<MyDocumentCategory> categories = categoryRepository.findAll();
        List<MyDocumentType> allTypes = typeRepository.findAll();
        List<MyEmployeeDocument> allDocs = employeeDocumentRepository.findByEmployeeId(emp.getId());

        List<MyDocumentCategoriesResponse.CategoryItem> items = categories.stream().map(cat -> {
            List<MyDocumentType> catTypes = allTypes.stream()
                    .filter(t -> t.getCategory().getId().equals(cat.getId()))
                    .collect(Collectors.toList());

            int total = catTypes.size();
            long uploaded = catTypes.stream()
                    .filter(t -> allDocs.stream().anyMatch(d -> d.getDocumentType().getId().equals(t.getId())))
                    .count();

            int percentage = total > 0 ? (int) ((uploaded * 100) / total) : 0;

            return new MyDocumentCategoriesResponse.CategoryItem(
                    cat.getId(), cat.getName(), cat.getIcon(), (int) uploaded, total, percentage
            );
        }).collect(Collectors.toList());

        return new MyDocumentCategoriesResponse(items);
    }

    @Transactional
    public MyCategoryDocumentsResponse getDocumentsByCategory(String email, Long categoryId) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyDocumentCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + categoryId));

        seedMockDocumentsForEmployee(emp);

        List<MyDocumentType> catTypes = typeRepository.findByCategoryId(categoryId);
        List<MyEmployeeDocument> allDocs = employeeDocumentRepository.findByEmployeeId(emp.getId());

        List<MyCategoryDocumentsResponse.DocumentItem> documents = catTypes.stream().map(type -> {
            Optional<MyEmployeeDocument> docOpt = allDocs.stream()
                    .filter(d -> d.getDocumentType().getId().equals(type.getId()))
                    .findFirst();

            if (docOpt.isPresent()) {
                MyEmployeeDocument doc = docOpt.get();
                MyCategoryDocumentsResponse.ActionInfo actions = new MyCategoryDocumentsResponse.ActionInfo(true, true, true, false);
                return new MyCategoryDocumentsResponse.DocumentItem(
                        doc.getId(),
                        type.getName(),
                        type.getCode(),
                        getDocumentStatus(doc),
                        doc.getVerificationStatus(),
                        doc.getExpiryDate(),
                        doc.getUpdatedAt(),
                        actions
                );
            } else {
                MyCategoryDocumentsResponse.ActionInfo actions = new MyCategoryDocumentsResponse.ActionInfo(false, false, false, true);
                return new MyCategoryDocumentsResponse.DocumentItem(
                        type.getId(), // Return type id as virtual document ID
                        type.getName(),
                        type.getCode(),
                        "NOT_UPLOADED",
                        null,
                        null,
                        null,
                        actions
                );
            }
        }).collect(Collectors.toList());

        return new MyCategoryDocumentsResponse(
                new MyCategoryDocumentsResponse.CategoryInfo(cat.getId(), cat.getName()),
                documents
        );
    }

    @Transactional
    public MyDocumentUploadResponse uploadDocument(String email, Long categoryId, String documentTypeCode, MultipartFile file, String documentNumber, LocalDate issuedDate, LocalDate expiryDate, String remarks) throws IOException {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyDocumentType type = typeRepository.findByCode(documentTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("Document type not found with code: " + documentTypeCode));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Document file is empty");
        }

        // Validate format
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toUpperCase() : "";
        boolean allowed = type.getAllowedFormats().stream().anyMatch(fmt -> filename.endsWith("." + fmt));
        if (!allowed && !type.getAllowedFormats().isEmpty()) {
            throw new IllegalArgumentException("File format not allowed. Supported formats: " + type.getAllowedFormats());
        }

        // Validate size
        long sizeInMB = file.getSize() / (1024 * 1024);
        if (sizeInMB > type.getMaxFileSizeInMB()) {
            throw new IllegalArgumentException("File size exceeds allowed maximum of " + type.getMaxFileSizeInMB() + " MB");
        }

        // Check duplicate
        Optional<MyEmployeeDocument> existing = employeeDocumentRepository.findByEmployeeIdAndDocumentTypeId(emp.getId(), type.getId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Document already exists. Use Replace/Update endpoint to upload a new version.");
        }

        MyEmployeeDocument doc = new MyEmployeeDocument();
        doc.setEmployee(emp);
        doc.setDocumentType(type);
        doc.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
        doc.setFileType(file.getContentType() != null ? file.getContentType() : "application/pdf");
        doc.setFileData(file.getBytes());
        doc.setFileSize(formatFileSize(file.getSize()));
        doc.setDocumentNumber(documentNumber);
        doc.setIssuedDate(issuedDate);
        doc.setExpiryDate(expiryDate);
        doc.setVersion(1);
        doc.setStatus("UPLOADED");
        doc.setVerificationStatus("PENDING_VERIFICATION");
        doc.setRemarks(remarks);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());

        MyEmployeeDocument saved = employeeDocumentRepository.save(doc);

        activityRepository.save(new MyDocumentActivity(
                emp, "DOCUMENT_UPLOADED", type.getName(), emp.getFullName(), LocalDateTime.now()
        ));

        return new MyDocumentUploadResponse(
                saved.getId(),
                saved.getFileName(),
                type.getCode(),
                saved.getVersion(),
                saved.getStatus(),
                saved.getVerificationStatus(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public MyDocumentReplaceResponse replaceDocument(String email, Long documentId, MultipartFile file, String remarks) throws IOException {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyEmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        if (!isAuthorizedToAccess(doc, email)) {
            throw new IllegalArgumentException("Access Denied: You do not have permission to access this document.");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Document file is empty");
        }

        MyDocumentType type = doc.getDocumentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toUpperCase() : "";
        boolean allowed = type.getAllowedFormats().stream().anyMatch(fmt -> filename.endsWith("." + fmt));
        if (!allowed && !type.getAllowedFormats().isEmpty()) {
            throw new IllegalArgumentException("File format not allowed. Supported formats: " + type.getAllowedFormats());
        }

        long sizeInMB = file.getSize() / (1024 * 1024);
        if (sizeInMB > type.getMaxFileSizeInMB()) {
            throw new IllegalArgumentException("File size exceeds allowed maximum of " + type.getMaxFileSizeInMB() + " MB");
        }

        int previousVersion = doc.getVersion();
        int newVersion = previousVersion + 1;

        doc.setVersion(newVersion);
        doc.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document_updated.pdf");
        doc.setFileType(file.getContentType() != null ? file.getContentType() : "application/pdf");
        doc.setFileData(file.getBytes());
        doc.setFileSize(formatFileSize(file.getSize()));
        doc.setStatus("UPLOADED");
        doc.setVerificationStatus("PENDING_VERIFICATION");
        doc.setRemarks(remarks);
        doc.setUpdatedAt(LocalDateTime.now());

        MyEmployeeDocument saved = employeeDocumentRepository.save(doc);

        activityRepository.save(new MyDocumentActivity(
                emp, "DOCUMENT_UPLOADED", type.getName(), emp.getFullName(), LocalDateTime.now()
        ));

        return new MyDocumentReplaceResponse(
                saved.getId(),
                previousVersion,
                newVersion,
                "UPDATED",
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public MyDocumentDetailsResponse getDocumentDetails(String email, Long documentId) {
        employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyEmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        if (!isAuthorizedToAccess(doc, email)) {
            throw new IllegalArgumentException("Access Denied: You do not have permission to access this document.");
        }

        MyDocumentDetailsResponse.VerificationInfo verification = new MyDocumentDetailsResponse.VerificationInfo(
                doc.getVerificationStatus(),
                doc.getVerifiedBy(),
                doc.getVerifiedAt(),
                doc.getVerificationRemarks()
        );

        return new MyDocumentDetailsResponse(
                doc.getId(),
                doc.getDocumentType().getName(),
                doc.getDocumentType().getCode(),
                doc.getDocumentType().getCategory().getName(),
                doc.getFileName(),
                doc.getFileType(),
                doc.getFileSize(),
                doc.getDocumentNumber(),
                doc.getIssuedDate(),
                doc.getExpiryDate(),
                doc.getVersion(),
                verification,
                doc.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public MyDocumentPreviewResponse previewDocument(String email, Long documentId) {
        employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyEmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        if (!isAuthorizedToAccess(doc, email)) {
            throw new IllegalArgumentException("Access Denied: You do not have permission to access this document.");
        }

        String previewUrl = "/api/v1/my-documents/" + documentId + "/download";
        return new MyDocumentPreviewResponse(
                doc.getId(),
                doc.getDocumentType().getName(),
                previewUrl,
                600
        );
    }

    @Transactional(readOnly = true)
    public MyEmployeeDocument downloadDocument(String email, Long documentId) {
        employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        MyEmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        if (!isAuthorizedToAccess(doc, email)) {
            throw new IllegalArgumentException("Access Denied: You do not have permission to access this document.");
        }

        return doc;
    }

    @Transactional
    public MyDocumentNotificationsResponse getExpiryNotifications(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        seedMockDocumentsForEmployee(emp);

        List<MyEmployeeDocument> allDocs = employeeDocumentRepository.findByEmployeeId(emp.getId());

        List<MyDocumentNotificationsResponse.NotificationItem> notifications = allDocs.stream()
                .filter(d -> d.getExpiryDate() != null)
                .filter(d -> "EXPIRED".equals(getDocumentStatus(d)) || "EXPIRING_SOON".equals(getDocumentStatus(d)))
                .map(d -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), d.getExpiryDate());
                    return new MyDocumentNotificationsResponse.NotificationItem(
                            d.getId(),
                            d.getDocumentType().getName(),
                            getDocumentStatus(d),
                            (int) days,
                            d.getExpiryDate()
                    );
                }).collect(Collectors.toList());

        return new MyDocumentNotificationsResponse(notifications);
    }

    @Transactional
    public MyDocumentHistoryResponse getDocumentActivityHistory(String email, int page, int size) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        seedMockDocumentsForEmployee(emp);

        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());
        Page<MyDocumentActivity> activityPage = activityRepository.findByEmployeeId(emp.getId(), pageable);

        List<MyDocumentHistoryResponse.HistoryItem> content = activityPage.getContent().stream()
                .map(act -> new MyDocumentHistoryResponse.HistoryItem(
                        act.getId(),
                        act.getAction(),
                        act.getDocumentName(),
                        act.getPerformedBy(),
                        act.getPerformedAt()
                )).collect(Collectors.toList());

        MyDocumentHistoryResponse.PaginationInfo pagination = new MyDocumentHistoryResponse.PaginationInfo(
                page, size, activityPage.getTotalElements(), activityPage.getTotalPages()
        );

        return new MyDocumentHistoryResponse(content, pagination);
    }

    @Transactional(readOnly = true)
    public MyDocumentTypesResponse getAllowedDocumentTypes() {
        List<MyDocumentType> types = typeRepository.findAll();
        List<MyDocumentTypesResponse.DocumentTypeItem> items = types.stream().map(type -> {
            return new MyDocumentTypesResponse.DocumentTypeItem(
                    type.getId(),
                    type.getCode(),
                    type.getName(),
                    type.getCategory().getName(),
                    type.isMandatory(),
                    type.isRequiresExpiryDate(),
                    type.getAllowedFormats(),
                    type.getMaxFileSizeInMB()
            );
        }).collect(Collectors.toList());

        return new MyDocumentTypesResponse(items);
    }
}
