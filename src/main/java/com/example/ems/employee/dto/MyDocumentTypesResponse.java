package com.example.ems.employee.dto;

import java.util.List;

public class MyDocumentTypesResponse {

    private List<DocumentTypeItem> documentTypes;

    public MyDocumentTypesResponse() {}

    public MyDocumentTypesResponse(List<DocumentTypeItem> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<DocumentTypeItem> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<DocumentTypeItem> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public static class DocumentTypeItem {
        private Long id;
        private String code;
        private String name;
        private String category;
        private boolean mandatory;
        private boolean requiresExpiryDate;
        private List<String> allowedFormats;
        private Integer maxFileSizeInMB;

        public DocumentTypeItem() {}

        public DocumentTypeItem(Long id, String code, String name, String category, boolean mandatory, boolean requiresExpiryDate) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.category = category;
            this.mandatory = mandatory;
            this.requiresExpiryDate = requiresExpiryDate;
        }

        public DocumentTypeItem(Long id, String code, String name, String category, boolean mandatory, boolean requiresExpiryDate, List<String> allowedFormats, Integer maxFileSizeInMB) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.category = category;
            this.mandatory = mandatory;
            this.requiresExpiryDate = requiresExpiryDate;
            this.allowedFormats = allowedFormats;
            this.maxFileSizeInMB = maxFileSizeInMB;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
        public boolean isRequiresExpiryDate() { return requiresExpiryDate; }
        public void setRequiresExpiryDate(boolean requiresExpiryDate) { this.requiresExpiryDate = requiresExpiryDate; }
        public List<String> getAllowedFormats() { return allowedFormats; }
        public void setAllowedFormats(List<String> allowedFormats) { this.allowedFormats = allowedFormats; }
        public Integer getMaxFileSizeInMB() { return maxFileSizeInMB; }
        public void setMaxFileSizeInMB(Integer maxFileSizeInMB) { this.maxFileSizeInMB = maxFileSizeInMB; }
    }
}
