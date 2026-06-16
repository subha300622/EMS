package com.example.ems.employee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MyCategoryDocumentsResponse {

    private CategoryInfo category;
    private List<DocumentItem> documents;

    public MyCategoryDocumentsResponse() {}

    public MyCategoryDocumentsResponse(CategoryInfo category, List<DocumentItem> documents) {
        this.category = category;
        this.documents = documents;
    }

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
    }

    public List<DocumentItem> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentItem> documents) {
        this.documents = documents;
    }

    public static class CategoryInfo {
        private Long id;
        private String name;

        public CategoryInfo() {}

        public CategoryInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class DocumentItem {
        private Long documentId;
        private String documentName;
        private String documentType;
        private String status;
        private String verificationStatus;
        private LocalDate expiryDate;
        private LocalDateTime lastUpdatedAt;
        private ActionInfo actions;

        public DocumentItem() {}

        public DocumentItem(Long documentId, String documentName, String documentType, String status, String verificationStatus, LocalDate expiryDate, LocalDateTime lastUpdatedAt, ActionInfo actions) {
            this.documentId = documentId;
            this.documentName = documentName;
            this.documentType = documentType;
            this.status = status;
            this.verificationStatus = verificationStatus;
            this.expiryDate = expiryDate;
            this.lastUpdatedAt = lastUpdatedAt;
            this.actions = actions;
        }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getVerificationStatus() { return verificationStatus; }
        public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
        public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
        public ActionInfo getActions() { return actions; }
        public void setActions(ActionInfo actions) { this.actions = actions; }
    }

    public static class ActionInfo {
        private Boolean canView;
        private Boolean canDownload;
        private Boolean canReplace;
        private Boolean canUpload;

        public ActionInfo() {}

        public ActionInfo(Boolean canView, Boolean canDownload, Boolean canReplace, Boolean canUpload) {
            this.canView = canView;
            this.canDownload = canDownload;
            this.canReplace = canReplace;
            this.canUpload = canUpload;
        }

        public Boolean getCanView() { return canView; }
        public void setCanView(Boolean canView) { this.canView = canView; }
        public Boolean getCanDownload() { return canDownload; }
        public void setCanDownload(Boolean canDownload) { this.canDownload = canDownload; }
        public Boolean getCanReplace() { return canReplace; }
        public void setCanReplace(Boolean canReplace) { this.canReplace = canReplace; }
        public Boolean getCanUpload() { return canUpload; }
        public void setCanUpload(Boolean canUpload) { this.canUpload = canUpload; }
    }
}
