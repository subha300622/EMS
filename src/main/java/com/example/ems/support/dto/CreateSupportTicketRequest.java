package com.example.ems.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateSupportTicketRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 250, message = "Subject cannot exceed 250 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "CategoryId is required")
    private Long categoryId;

    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    private String source = "WEB";

    private List<SupportAttachmentRefDto> attachments;

    public CreateSupportTicketRequest() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<SupportAttachmentRefDto> getAttachments() { return attachments; }
    public void setAttachments(List<SupportAttachmentRefDto> attachments) { this.attachments = attachments; }
}
