package com.example.ems.support.dto;

import java.util.List;

public class CreateTicketRequest {
    private Long categoryId;
    private Long subCategoryId;
    private String subject;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private List<AttachmentRef> attachments;
    private String preferredContactMethod; // EMAIL, PHONE

    public CreateTicketRequest() {}

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getSubCategoryId() { return subCategoryId; }
    public void setSubCategoryId(Long subCategoryId) { this.subCategoryId = subCategoryId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public List<AttachmentRef> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentRef> attachments) { this.attachments = attachments; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public static class AttachmentRef {
        private String fileId;

        public AttachmentRef() {}

        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
    }
}
