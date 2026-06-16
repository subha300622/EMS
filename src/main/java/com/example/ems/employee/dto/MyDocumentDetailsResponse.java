package com.example.ems.employee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MyDocumentDetailsResponse {

    private Long documentId;
    private String documentName;
    private String documentType;
    private String category;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String documentNumber;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private int version;
    private VerificationInfo verification;
    private LocalDateTime createdAt;

    public MyDocumentDetailsResponse() {}

    public MyDocumentDetailsResponse(Long documentId, String documentName, String documentType, String category, String fileName, String fileType, String fileSize, String documentNumber, LocalDate issuedDate, LocalDate expiryDate, int version, VerificationInfo verification, LocalDateTime createdAt) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentType = documentType;
        this.category = category;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.documentNumber = documentNumber;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
        this.version = version;
        this.verification = verification;
        this.createdAt = createdAt;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public VerificationInfo getVerification() { return verification; }
    public void setVerification(VerificationInfo verification) { this.verification = verification; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class VerificationInfo {
        private String status;
        private String verifiedBy;
        private LocalDateTime verifiedAt;
        private String remarks;

        public VerificationInfo() {}

        public VerificationInfo(String status, String verifiedBy, LocalDateTime verifiedAt, String remarks) {
            this.status = status;
            this.verifiedBy = verifiedBy;
            this.verifiedAt = verifiedAt;
            this.remarks = remarks;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getVerifiedBy() { return verifiedBy; }
        public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
}
