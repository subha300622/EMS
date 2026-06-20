package com.example.ems.settings.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class SupportTicketRequest {

    @NotBlank(message = "Category is required")
    @Schema(example = "string")
    private String category;

    @NotBlank(message = "Subject is required")
    @Schema(example = "Request for Leave")
    private String subject;

    @NotBlank(message = "Description is required")
    @Schema(example = "Detailed description of the item")
    private String description;

    public SupportTicketRequest() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
