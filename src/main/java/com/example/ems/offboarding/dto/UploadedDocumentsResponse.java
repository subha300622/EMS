package com.example.ems.offboarding.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UploadedDocumentsResponse {

    private List<DocumentItem> documents;

    public UploadedDocumentsResponse() {}

    public UploadedDocumentsResponse(List<DocumentItem> documents) {
        this.documents = documents;
    }

    public List<DocumentItem> getDocuments() { return documents; }
    public void setDocuments(List<DocumentItem> documents) { this.documents = documents; }

    public static class DocumentItem {
        private Long documentId;
        private String documentType;
        private String fileName;
        private String status;
        private String verifiedBy;
        private LocalDateTime verifiedAt;

        public DocumentItem() {}

        public DocumentItem(Long documentId, String documentType, String fileName, String status, String verifiedBy, LocalDateTime verifiedAt) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.fileName = fileName;
            this.status = status;
            this.verifiedBy = verifiedBy;
            this.verifiedAt = verifiedAt;
        }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }

        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVerifiedBy() { return verifiedBy; }
        public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    }
}
